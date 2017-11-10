/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uh.hulib.attx.wc.uv.t.rmlservice;

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
import org.uh.hulib.attx.wc.uv.common.pojos.ProvenanceMessage;
import org.uh.hulib.attx.wc.uv.common.pojos.RMLServiceInput;
import org.uh.hulib.attx.wc.uv.common.pojos.RMLServiceRequest;
import org.uh.hulib.attx.wc.uv.common.pojos.RMLServiceResponse;
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
public class RMLService extends AbstractDpu<RMLServiceConfig_V1> {

    public static final String datasetURISymbolicName = "datasetURI";

    private static final Logger log = LoggerFactory.getLogger(RMLService.class);

    private long workflowID;
    private long executionID;
    private long stepID;

    @DataUnit.AsInput(name = "fileInput", optional = true)
    public FilesDataUnit filesInput;

    @DataUnit.AsInput(name = "uriInput", optional = true)
    public RDFDataUnit uriInput;

    @DataUnit.AsOutput(name = "datasetURI")
    public WritableRDFDataUnit datasetURI;

    @ExtensionInitializer.Init(param = "datasetURI")
    public WritableSimpleRdf rdfData;

    public RMLService() {
        super(RMLServiceVaadinDialog.class, ConfigHistory.noHistory(RMLServiceConfig_V1.class));
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

        ContextUtils.sendShortInfo(ctx, "RMLService.message");
        this.stepID = ctx.getExecMasterContext().getDpuContext().getDpuInstanceId();
        this.workflowID = ctx.getExecMasterContext().getDpuContext().getPipelineId();
        this.executionID = ctx.getExecMasterContext().getDpuContext().getPipelineExecutionId();
        RepositoryConnection c = null;
        RabbitMQClient mq = null;

        try {
            c = uriInput.getConnection();

            Set<FilesDataUnit.Entry> fileEntries = FilesHelper.getFiles(filesInput);
            org.openrdf.model.URI[] uriEntries = RDFHelper.getGraphsURIArray(uriInput);
            if (fileEntries.size() > 0 || uriEntries.length > 0) {
                System.out.println("- RML conf:");
                System.out.println(config.getConfiguration());

                mq = new RabbitMQClient("messagebroker", System.getenv("MUSER"), System.getenv("MPASS"), "provenance.inbox");
                ObjectMapper mapper = new ObjectMapper();
                Provenance prov = getProv();
                List<Source> files = new ArrayList<Source>();
                try {
                    while (fileEntries.iterator().hasNext()) {
                        Source s = new Source();
                        s.setInputType("Data");
                        s.setInput(FileUtils.readFileToString(new File(new URI(fileEntries.iterator().next().getFileURIString())), "UTF-8"));
                    }
                    for (org.openrdf.model.URI graphURI : uriEntries) {
                        String inputURI = getSinglePropertyValue(c, graphURI, c.getValueFactory().createURI("http://hulib.helsinki.fi/attx/uv/dpu/fileURI"));
                        Source s = new Source();
                        s.setInputType("URI");
                        s.setInput(inputURI);
                    }
                    RMLServiceRequest request = new RMLServiceRequest();
                    Provenance requestProv = new Provenance();
                    requestProv.setContext(getProvenanceContext());
                    request.setProvenance(requestProv);
                    RMLServiceInput requestInput = new RMLServiceInput();
                    requestInput.setMapping(config.getConfiguration());
                    requestInput.setSourceData(files);
                    request.setPayload(requestInput);
                    String responseText = mq.sendSyncServiceMessage(mapper.writeValueAsString(request), "rmlservice", 10000);
                    if (responseText == null) {
                        throw new Exception("No response from service!");
                    }

                    RMLServiceResponse response = mapper.readValue(responseText, RMLServiceResponse.class);
                    if (!response.getPayload().getStatus().equals("SUCCESS")) {
                        throw new Exception("Transformation failed. " + response.getPayload().getStatusMessage());
                    }
                    System.out.println(response.getPayload().getTransformedDatasetURL());
                    prov.getActivity().setStatus("SUCCESS");

                    ProvenanceMessage provMsg = new ProvenanceMessage();
                    provMsg.setProvenance(prov);
                    mq.sendProvMessage(mapper.writeValueAsString(provMsg));

                    final ValueFactory vf = rdfData.getValueFactory();
                    // entry is the graph 
                    final RDFDataUnit.Entry entry = RdfDataUnitUtils.addGraph(
                            datasetURI,
                            DataUnitUtils.generateSymbolicName(RMLService.class));

                    final EntityBuilder datasetUriEntity = new EntityBuilder(vf.createURI("http://hulib.helsinki.fi/attx/uv/dpu/RMLService"), vf);
                    datasetUriEntity.property(vf.createURI("http://hulib.helsinki.fi/attx/uv/dpu/fileURI"), vf.createURI(response.getPayload().getTransformedDatasetURL()));

                    rdfData.setOutput(entry);
                    rdfData.add(datasetUriEntity.asStatements());

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
                            java.util.logging.Logger.getLogger(RMLService.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (mq != null) {
                        try {
                            mq.close();
                        } catch (IOException ex) {
                            java.util.logging.Logger.getLogger(RMLService.class.getName()).log(Level.SEVERE, null, ex);
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
