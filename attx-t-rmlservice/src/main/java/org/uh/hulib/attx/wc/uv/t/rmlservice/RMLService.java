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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.uh.hulib.attx.wc.uv.common.pojos.RMLServiceRequestMessage;
import org.uh.hulib.attx.wc.uv.common.pojos.RMLServiceResponseMessage;
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

    private void addInputs(RepositoryConnection c, ProvenanceMessage msg, org.openrdf.model.URI[] uriEntries, Set<FilesDataUnit.Entry> fileEntries) throws Exception {
        Map<String, Object> payload = msg.getPayload();
        if(payload == null) {
            payload = new HashMap<String, Object>();
        }
        Provenance prov = msg.getProvenance();
        if(prov == null) {
            throw new Exception("Provenance needs to be set before calling addInputsAndOutputs");
        }


        int p = 0;
        // URI inputs 
        prov.setInput(new ArrayList<DataProperty>());        
        if (uriEntries.length > 0) {
            for(int i = 0; i < uriEntries.length; i++) {
                org.openrdf.model.URI g = uriEntries[i];  
                String inputURI = getSinglePropertyValue(c, g, c.getValueFactory().createURI("http://hulib.helsinki.fi/attx/uv/dpu/fileURI"));
                  
                DataProperty input = new DataProperty();
                input.setKey("inputDataset" + p);
                input.setRole("Dataset");
                prov.getInput().add(input);                
                payload.put("inputDataset" + p, inputURI);                
                p++;
            }
        } 
        
        // file inputs
        Iterator<FilesDataUnit.Entry> fileIterator = fileEntries.iterator();
        while (fileIterator.hasNext()) {
            DataProperty input = new DataProperty();
            input.setKey("inputDataset" + p);
            input.setRole("Dataset");
            prov.getInput().add(input);                
            payload.put("inputDataset" + p, fileIterator.next().getFileURIString());                
            p++;
        }        
        
        
        msg.setPayload(payload);
        
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
                Map<String, Object> provPayload = new HashMap<String, Object>();

                List<Source> files = new ArrayList<Source>();
                try {
                    Iterator<FilesDataUnit.Entry> fileIterator = fileEntries.iterator();
                    while (fileIterator.hasNext()) {                        
                        Source s = new Source();
                        s.setInputType("Data");
                        s.setInput(FileUtils.readFileToString(new File(new URI(fileIterator.next().getFileURIString())), "UTF-8"));
                        files.add(s);
                    }
                    
                    for (org.openrdf.model.URI graphURI : uriEntries) {
                        String inputURI = getSinglePropertyValue(c, graphURI, c.getValueFactory().createURI("http://hulib.helsinki.fi/attx/uv/dpu/fileURI"));
                        Source s = new Source();
                        s.setInputType("URI");
                        s.setInput(inputURI);
                        files.add(s);
                    }
                                        
                    RMLServiceRequestMessage request = new RMLServiceRequestMessage();
                    Provenance requestProv = new Provenance();
                    requestProv.setContext(getProvenanceContext());
                    request.setProvenance(requestProv);
                    RMLServiceInput requestInput = new RMLServiceInput();
                    requestInput.setRmlMapping(config.getConfiguration());
                    requestInput.setSourceData(files);
                    RMLServiceRequestMessage.RMLServiceRequestPayload payload = request.new RMLServiceRequestPayload();
                    payload.setRMLServiceInput(requestInput);
                    request.setPayload(payload);
                    String requestStr = mapper.writeValueAsString(request);
                    log.debug(requestStr);
                    String responseText = mq.sendSyncServiceMessage(requestStr, "rmlservice", 600000);
                    if (responseText == null) {
                        throw new Exception("No response from service!");
                    }

                    log.debug(responseText);
                    RMLServiceResponseMessage response = mapper.readValue(responseText, RMLServiceResponseMessage.class);
                    if (!response.getPayload().getStatus().equals("success")) {
                        throw new Exception("Transformation failed. " + response.getPayload().getStatusMessage());
                    }
                    prov.getActivity().setStatus("success");
                    prov.setOutput(new ArrayList<DataProperty>());                


                    final ValueFactory vf = rdfData.getValueFactory();
                    // entry is the graph 
                    final RDFDataUnit.Entry entry = RdfDataUnitUtils.addGraph(
                            datasetURI,
                            DataUnitUtils.generateSymbolicName(RMLService.class));

                    rdfData.setOutput(entry);

                    int i = 0;
                    for(String uri : response.getPayload().getRMLServiceOutput().getOutput()) {
                        final EntityBuilder datasetUriEntity = new EntityBuilder(vf.createURI("http://hulib.helsinki.fi/attx/uv/dpu/RMLService"), vf);                        
                        datasetUriEntity.property(vf.createURI("http://hulib.helsinki.fi/attx/uv/dpu/fileURI"), vf.createURI(uri));
                        String contentType = response.getPayload().getRMLServiceOutput().getContentType();
                        datasetUriEntity.property(vf.createURI("http://hulib.helsinki.fi/attx/uv/dpu/fileContentType"), vf.createLiteral(contentType));
                        
                        rdfData.add(datasetUriEntity.asStatements());
                        
                        DataProperty output = new DataProperty();
                        output.setKey("outputDataset" + i);
                        output.setRole("Dataset");            
                        prov.getOutput().add(output);
                        provPayload.put("outputDataset" + i, uri);                            
                        i++;
                    }
                    
                    ProvenanceMessage provMsg = new ProvenanceMessage();
                    provMsg.setProvenance(prov);
                    provMsg.setPayload(provPayload);
                    addInputs(c, provMsg, uriEntries, fileEntries);
                    String provStepMessage = mapper.writeValueAsString(provMsg);
                    log.debug(provStepMessage);
                    mq.sendProvMessage(provStepMessage);


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
        provAgent.setID("UnifiedViews");
        provAgent.setRole("ETL");

        Activity provAct = new Activity();
        provAct.setTitle("Transform to RDF");
        provAct.setType("StepExecution");

        Communication provCom = new Communication();
        provCom.setRole("transformer");
        provCom.setAgent("rmlservice");
        provCom.setInput(new ArrayList<DataProperty>());

        provAct.setCommunication(new ArrayList<Communication>());
        provAct.getCommunication().add(provCom);

        
        provContent.setContext(provContext);
        provContent.setActivity(provAct);
        provContent.setAgent(provAgent);
        
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
