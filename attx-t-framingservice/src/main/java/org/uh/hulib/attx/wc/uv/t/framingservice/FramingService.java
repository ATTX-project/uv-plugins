/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uh.hulib.attx.wc.uv.t.framingservice;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.DataUnitUtils;
import eu.unifiedviews.helpers.dataunit.files.FilesHelper;
import eu.unifiedviews.helpers.dataunit.rdf.RDFHelper;
import eu.unifiedviews.helpers.dataunit.rdf.RdfDataUnitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;

import eu.unifiedviews.helpers.dpu.extension.rdf.simple.WritableSimpleRdf;
import eu.unifiedviews.helpers.dpu.rdf.EntityBuilder;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.apache.commons.io.FileUtils;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.uh.hulib.attx.wc.uv.common.MessagingClient;
import org.uh.hulib.attx.wc.uv.common.RabbitMQClient;
import org.uh.hulib.attx.wc.uv.common.pojos.ConstructRequestMessage;
import org.uh.hulib.attx.wc.uv.common.pojos.FramingRequestMessage;
import org.uh.hulib.attx.wc.uv.common.pojos.FramingResponseMessage;
import org.uh.hulib.attx.wc.uv.common.pojos.FramingServiceInput;
import org.uh.hulib.attx.wc.uv.common.pojos.ProvenanceMessage;
import org.uh.hulib.attx.wc.uv.common.pojos.RMLServiceInput;
import org.uh.hulib.attx.wc.uv.common.pojos.Source;
import org.uh.hulib.attx.wc.uv.common.pojos.prov.Activity;
import org.uh.hulib.attx.wc.uv.common.pojos.prov.Agent;
import org.uh.hulib.attx.wc.uv.common.pojos.prov.Communication;
import org.uh.hulib.attx.wc.uv.common.pojos.prov.Context;
import org.uh.hulib.attx.wc.uv.common.pojos.prov.DataProperty;
import org.uh.hulib.attx.wc.uv.common.pojos.prov.Provenance;

/**
 * Main data processing unit class.
 *
 * @author Unknown
 */
@DPU.AsTransformer
public class FramingService extends AbstractDpu<FramingServiceConfig_V1> {

    public static final String datasetURISymbolicName = "datasetURI";

    private static final Logger log = LoggerFactory.getLogger(FramingService.class);

    private long workflowID;
    private long executionID;
    private long stepID;

    @DataUnit.AsInput(name = "fileInput", optional = true)
    public FilesDataUnit filesInput;

    @DataUnit.AsInput(name = "uriInput", optional = true)
    public RDFDataUnit uriInput;

    @DataUnit.AsOutput(name = "fileURI")
    public WritableRDFDataUnit fileURI;

    @ExtensionInitializer.Init(param = "fileURI")
    public WritableSimpleRdf fileData;

    public FramingService() {
        super(FramingServiceVaadinDialog.class, ConfigHistory.noHistory(FramingServiceConfig_V1.class));
    }

    private Context getProvenanceContext() throws Exception {
        Context ctx = new Context();
        ctx.setActivityID("" + executionID);
        ctx.setStepID("" + stepID);
        ctx.setWorkflowID("" + workflowID);

        return ctx;
    }

    @Override
    protected void innerExecute() throws DPUException {

        ContextUtils.sendShortInfo(ctx, "FramingService starting");
        this.stepID = ctx.getExecMasterContext().getDpuContext().getDpuInstanceId();
        this.workflowID = ctx.getExecMasterContext().getDpuContext().getPipelineId();
        this.executionID = ctx.getExecMasterContext().getDpuContext().getPipelineExecutionId();
        RepositoryConnection c = null;
        RabbitMQClient mq = null;

        try {             
            org.openrdf.model.URI[] uriEntries = new org.openrdf.model.URI[0];
            Set<FilesDataUnit.Entry> fileEntries = new HashSet<FilesDataUnit.Entry>();
            if(uriInput != null) {                
                c = uriInput.getConnection();
                uriEntries = RDFHelper.getGraphsURIArray(uriInput);
            }
            else {               
                c = filesInput.getConnection();
                fileEntries = FilesHelper.getFiles(filesInput);
            }
            
            if (fileEntries.size() > 0 || uriEntries.length > 0) {
                System.out.println("- RML conf:");
                System.out.println(config.getConfiguration());

                mq = new RabbitMQClient("messagebroker", System.getenv("MUSER"), System.getenv("MPASS"), "provenance.inbox");
                ObjectMapper mapper = new ObjectMapper();
                Provenance prov = getProv();
                List<Source> files = new ArrayList<Source>();
                try {
                    Iterator<FilesDataUnit.Entry> fileIterator = fileEntries.iterator();
                    while (fileIterator.hasNext()) {
                        Source s = new Source();
                        s.setInputType("Data");
                        s.setInput(FileUtils.readFileToString(new File(new URI(fileIterator.next().getFileURIString())), "UTF-8"));
                        files.add(s);
                    }
                    log.info("Read data inputs");
                    for (org.openrdf.model.URI graphURI : uriEntries) {
                        String inputURI = getSinglePropertyValue(c, graphURI, c.getValueFactory().createURI("http://hulib.helsinki.fi/attx/uv/dpu/fileURI"));
                        Source s = new Source();
                        s.setInputType("URI");
                        s.setInput(inputURI);
                        files.add(s);
                    }
                    log.info("Read uri inputs");
                    FramingRequestMessage request = new FramingRequestMessage();
                    Provenance requestProv = new Provenance();
                    requestProv.setContext(getProvenanceContext());
                    request.setProvenance(requestProv);
                    FramingServiceInput requestInput = new FramingServiceInput();
                    requestInput.setDocType(config.getDocType());
                    requestInput.setLdFrame(config.getConfiguration());
                    requestInput.setSourceData(files);
                    
                    FramingRequestMessage.FramingRequestMessagePayload p = request.new FramingRequestMessagePayload();                    
                    p.setGraphManagerInput(requestInput);
                    request.setPayload(p);
                    
                    String requestStr = mapper.writeValueAsString(request);
                    log.info(requestStr);
                    String responseText = mq.sendSyncServiceMessage(requestStr, "attx.ldframe.inbox", 60000);
                    if (responseText == null) {
                        throw new Exception("No response from service!");
                    }

                    log.info(responseText);
                    FramingResponseMessage response = mapper.readValue(responseText, FramingResponseMessage.class);
                    
                    
                    if (!response.getPayload().getStatus().equalsIgnoreCase("success")) {
                        throw new Exception("Transformation failed. " + response.getPayload().getStatusMessage());
                    }
                    prov.getActivity().setStatus("SUCCESS");
                    ProvenanceMessage provMsg = new ProvenanceMessage();
                    provMsg.setProvenance(prov);
                    mq.sendProvMessage(mapper.writeValueAsString(provMsg));

                    final ValueFactory vf = fileData.getValueFactory();
                    // entry is the graph 
                    final RDFDataUnit.Entry entry = RdfDataUnitUtils.addGraph(fileURI,
                            DataUnitUtils.generateSymbolicName(FramingService.class));

                    fileData.setOutput(entry);

                    String resultURI = response.getPayload().getFramingServiceOutput().getOutput();
                    String contentType = response.getPayload().getFramingServiceOutput().getContentType();
                    // TODO: what if the the result type if data?
                    final EntityBuilder datasetUriEntity = new EntityBuilder(vf.createURI("http://hulib.helsinki.fi/attx/uv/dpu/FramingService"), vf);
                    datasetUriEntity.property(vf.createURI("http://hulib.helsinki.fi/attx/uv/dpu/fileURI"), vf.createURI(resultURI));
                    datasetUriEntity.property(vf.createURI("http://hulib.helsinki.fi/attx/uv/dpu/fileContentType"), vf.createLiteral(contentType));

                    fileData.add(datasetUriEntity.asStatements());
                    


                } catch (Exception ex) {
                    ex.printStackTrace();
                    prov.getActivity().setStatus("FAILED");
                    mq.sendProvMessage(mapper.writeValueAsString(prov));
                    ContextUtils.sendError(ctx, "Error occured.", ex.getMessage());
                } finally {
                    if (c != null) {
                        try {
                            c.close();
                        } catch (RepositoryException ex) {
                            java.util.logging.Logger.getLogger(FramingService.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (mq != null) {
                        try {
                            mq.close();
                        } catch (IOException ex) {
                            java.util.logging.Logger.getLogger(FramingService.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Provenance getProv() throws Exception {

        Provenance provContent = new Provenance();
        Context provContext = getProvenanceContext();

        Agent provAgent = new Agent();
        provAgent.setID("UV");
        provAgent.setRole("ETL");

        Activity provAct = new Activity();
        provAct.setTitle("Transform to RDF");
        provAct.setType("StepExecution");

        Communication provCom = new Communication();
        provCom.setRole("transformer");
        provCom.setAgent("RMLService");
        provCom.setInput(new ArrayList<DataProperty>());

        provAct.setCommunication(new ArrayList<Communication>());
        provAct.getCommunication().add(provCom);

        DataProperty provInput = new DataProperty();
        provInput.setKey("harvestedContent");
        DataProperty provOutput = new DataProperty();
        provOutput.setKey("transformerData");
        provOutput.setRole("tempDataset");

        provContent.setContext(provContext);
        provContent.setAgent(provAgent);
        provContent.setActivity(provAct);
        provContent.setInput(new ArrayList());
        provContent.getInput().add(provInput);
        provContent.setOutput(new ArrayList());
        provContent.getOutput().add(provOutput);
        return provContent;
    }

    private String getSinglePropertyValue(RepositoryConnection c, org.openrdf.model.URI graph, org.openrdf.model.URI prop) throws Exception {
        RepositoryResult<Statement> r = c.getStatements(null, prop, null, false, graph);
        if (r.hasNext()) {
            Statement stmt = r.next();
            return stmt.getObject().stringValue();
        } else {
            return null;
        }

    }  
}