/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uh.hulib.attx.wc.uv.l.replaceds;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.files.FilesHelper;
import eu.unifiedviews.helpers.dataunit.rdf.RDFHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.DC;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.uh.hulib.attx.wc.uv.common.MessagingClient;
import org.uh.hulib.attx.wc.uv.common.RabbitMQClient;
import org.uh.hulib.attx.wc.uv.common.pojos.GraphManagerInput;
import org.uh.hulib.attx.wc.uv.common.pojos.ProvenanceMessage;
import org.uh.hulib.attx.wc.uv.common.pojos.ReplaceDSRequestMessage;
import org.uh.hulib.attx.wc.uv.common.pojos.ReplaceDSResponseMessage;
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
@DPU.AsLoader
public class ReplaceDS extends AbstractDpu<ReplaceDSConfig_V1> {

    private static final Logger log = LoggerFactory.getLogger(ReplaceDS.class);
    

    @DataUnit.AsInput(name = "outputDatasetMetadata", optional = false)
    public RDFDataUnit outputDatasetMetadata;

    @DataUnit.AsInput(name = "inputDatasetMetadata", optional = true)
    public RDFDataUnit inputDatasetMetadata;

    @DataUnit.AsInput(name = "dataURI", optional = true)
    public RDFDataUnit dataURIs;

    @DataUnit.AsInput(name = "datasetFiles", optional = true)
    public FilesDataUnit dataFiles;

    
    private long workflowID;
    private long executionID;
    private long stepID;
    
    public ReplaceDS() {
        super(ReplaceDSVaadinDialog.class, ConfigHistory.noHistory(ReplaceDSConfig_V1.class));
    }

    private String getOutputGraphURI() throws Exception {
        return "http://data.hulib.helsinki.fi/attx/work/wf_" + workflowID + "_step_" + stepID + "/output"; 
    }
        
    private Context getProvenanceContext() throws Exception {
        Context ctx = new Context();
        ctx.setActivityID("" +executionID);
        ctx.setStepID("" + stepID);
        ctx.setWorkflowID("" + workflowID);
                
        return ctx;
    }  
    
    private ProvenanceMessage getWorkflowExecutedMessage(RepositoryConnection c) throws Exception {
        ProvenanceMessage provMessage = new ProvenanceMessage();
        Provenance prov = new Provenance();
        Agent provAgent = new Agent();
        provAgent.setID("UnifiedViews");
        provAgent.setRole("ETL");
        prov.setAgent(provAgent);

        Activity provAct = new Activity();
        //provAct.setTitle("TODO: name of the workflow should be here");
        provAct.setType("WorkflowExecution");
        prov.setActivity(provAct);
        prov.setContext(getProvenanceContext());        
        prov.getContext().setStepID("");
        
        provMessage.setProvenance(prov);
        
        addInputsAndOutputs(c, provMessage);
        return provMessage;
    }
    private ProvenanceMessage getWorkflowDescribeMessage(RepositoryConnection c) throws Exception {
        ProvenanceMessage provMessage = new ProvenanceMessage();
        // outputDatasetMetadata is required 


        Map<String, Object> payload = new HashMap<String, Object>();

        Provenance prov = new Provenance();
        Agent provAgent = new Agent();
        provAgent.setID("UnifiedViews");
        provAgent.setRole("ETL");
        prov.setAgent(provAgent);

        Activity provAct = new Activity();
        //provAct.setTitle("TODO: name of the workflow should be here");
        provAct.setType("DescribeStepExecution");
        prov.setActivity(provAct);
        prov.setContext(getProvenanceContext());
        
        URI[] outputGraphsMetadata = RDFHelper.getGraphsURIArray(outputDatasetMetadata);
        
        
        if (outputGraphsMetadata.length > 0) {
            // just using the first one for now!
            URI g = outputGraphsMetadata[0];
            
            writeGraph(c, g, System.out);
            
            String targetGraph = getOutputGraphURI();
            String targetGraphTitle = getSinglePropertyValue(c, g, DC.TITLE);
            String targetGraphDesc = getSinglePropertyValue(c, g, DC.DESCRIPTION);

            DataProperty output = new DataProperty();
            output.setKey("outputDataset");
            output.setRole("Dataset");
            prov.setOutput(new ArrayList<DataProperty>());
            prov.getOutput().add(output);

            Map<String, Object> outputDataset = new HashMap<String, Object>();
            outputDataset.put("uri", targetGraph);
            outputDataset.put("title", targetGraphTitle);
            outputDataset.put("description", targetGraphDesc);

            payload.put("outputDataset", outputDataset);
        }

        URI[] inputGraphsMetadata = RDFHelper.getGraphsURIArray(inputDatasetMetadata);
        prov.setInput(new ArrayList<DataProperty>());
        
        if (inputGraphsMetadata.length > 0) {
            for(int i = 0; i < inputGraphsMetadata.length; i++) {
                URI g = inputGraphsMetadata[i];            
                //writeGraph(c, g, System.out);
                        
                String sourceGraph = g.toString();
                String sourceGraphTitle = getSinglePropertyValue(c, g, DC.TITLE);
                String sourceGraphDesc = getSinglePropertyValue(c, g, DC.DESCRIPTION);
                String sourceGraphPublisher = getSinglePropertyValue(c, g, DC.PUBLISHER);
                String sourceGraphLicense = getSinglePropertyValue(c, g, DC.RIGHTS);

                DataProperty input = new DataProperty();
                input.setKey("inputDataset" + i);
                input.setRole("Dataset");
                prov.getInput().add(input);

            
                Map<String, Object> inputDataset = new HashMap<String, Object>();
                inputDataset.put("uri", sourceGraph);
                if(sourceGraphTitle != null)
                    inputDataset.put("title", sourceGraphTitle);
                if(sourceGraphDesc != null)
                    inputDataset.put("description", sourceGraphDesc);
                if(sourceGraphPublisher != null)
                    inputDataset.put("publisher", sourceGraphPublisher);
                if(sourceGraphLicense != null)
                    inputDataset.put("license", sourceGraphLicense);

                payload.put("inputDataset" + i, inputDataset);
            }

        }
        // check for existing datasets         
        
        provMessage.setProvenance(prov);
        provMessage.setPayload(payload);

        
        
        return provMessage;

    }
    
    private void addInputsAndOutputs(RepositoryConnection c, ProvenanceMessage msg) throws Exception {
        Map<String, Object> payload = msg.getPayload();
        if(payload == null) {
            payload = new HashMap<String, Object>();
        }
        Provenance prov = msg.getProvenance();
        if(prov == null) {
            throw new Exception("Provenance needs to be set before calling addInputsAndOutputs");
        }
        URI[] outputGraphsMetadata = RDFHelper.getGraphsURIArray(outputDatasetMetadata);
        prov.setOutput(new ArrayList<DataProperty>());                
        if (outputGraphsMetadata.length > 0) {
            DataProperty output = new DataProperty();
            output.setKey("outputDataset");
            output.setRole("Dataset");            
            prov.getOutput().add(output);
            payload.put("outputDataset", getOutputGraphURI());            
        }

        URI[] inputGraphsMetadata = RDFHelper.getGraphsURIArray(dataURIs);
        prov.setInput(new ArrayList<DataProperty>());        
        if (inputGraphsMetadata.length > 0) {
            for(int i = 0; i < inputGraphsMetadata.length; i++) {
                URI g = inputGraphsMetadata[i];    
                String inputURI = getSinglePropertyValue(c, g, c.getValueFactory().createURI("http://hulib.helsinki.fi/attx/uv/dpu/fileURI"));

                DataProperty input = new DataProperty();
                input.setKey("inputDataset" + i);
                input.setRole("Dataset");
                prov.getInput().add(input);
                
                payload.put("inputDataset" + i, inputURI);                
            }
        } 
        
        msg.setPayload(payload);
        
    }

    @Override
    protected void innerExecute() throws DPUException {

        ContextUtils.sendShortInfo(ctx, "ReplaceDS.message");
        this.stepID = ctx.getExecMasterContext().getDpuContext().getDpuInstanceId();
        this.workflowID = ctx.getExecMasterContext().getDpuContext().getPipelineId();
        this.executionID = ctx.getExecMasterContext().getDpuContext().getPipelineExecutionId();

        RepositoryConnection c = null;
        RabbitMQClient mq = null;
        try {
            mq = new RabbitMQClient("messagebroker", System.getenv("MUSER"), System.getenv("MPASS"), "provenance.inbox");
            ObjectMapper mapper = new ObjectMapper();
            ProvenanceMessage provMessageStep = new ProvenanceMessage();            
            Provenance stepProv = getStepProv();
            provMessageStep.setProvenance(stepProv);
            
            try {

                c = outputDatasetMetadata.getConnection();

                // create dataset related prov content
                addInputsAndOutputs(c, provMessageStep);
                

                Set<FilesDataUnit.Entry> fileEntries = FilesHelper.getFiles(dataFiles);
                System.out.println("datasetFiles size:" + fileEntries.size());

                URI[] uriEntries = RDFHelper.getGraphsURIArray(dataURIs);
                System.out.println("dataSetURIs size:" + uriEntries.length);

                ReplaceDSRequestMessage request = new ReplaceDSRequestMessage();
                ReplaceDSRequestMessage.ReplaceDSRequestPayload p = request.new ReplaceDSRequestPayload();

                

                if (uriEntries.length > 0) {
                    GraphManagerInput graphManagerInput = new GraphManagerInput();
                    graphManagerInput.setTask(config.getGraphActivity());
                    graphManagerInput.setTargetGraph(getOutputGraphURI());
                    graphManagerInput.setSourceData(new ArrayList());

                    for (URI graphURI : uriEntries) {
                        writeGraph(c, graphURI, System.out);
                        // check for file URI
                        String inputURI = getSinglePropertyValue(c, graphURI, c.getValueFactory().createURI("http://hulib.helsinki.fi/attx/uv/dpu/fileURI"));
                        if (inputURI != null) {
                            Source source = new Source();

                            String contentType = getSinglePropertyValue(c, graphURI, c.getValueFactory().createURI("http://hulib.helsinki.fi/attx/uv/dpu/fileContentType"));
                            if(contentType == null) {
                                throw new Exception("No content type found the input data file!");
                            }
                            source.setContentType(contentType);

                            source.setInputType("URI");
                            source.setInput(inputURI);
                            
                            graphManagerInput.getSourceData().add(source);
                        }
                        else {
                            //ContextUtils.sendError(ctx, "No input dataset files available.", );
                        }
                    }                    
                    p.setGraphManagerInput(graphManagerInput);
                    request.setPayload(p);
                    Provenance requestProv = new Provenance();
                    requestProv.setContext(getProvenanceContext());
                    request.setProvenance(requestProv);
                    
                    String requestStr = mapper.writeValueAsString(request);
                    log.info(requestStr);
                    String responseStr = mq.sendSyncServiceMessage(requestStr, "attx.graphManager.inbox", 60000);
                    
                    log.info(responseStr);
                    if(responseStr == null) {
                        throw new Exception("Null response from service");                    
                    }

                    //TODO: get the status from response
                    ReplaceDSResponseMessage response = mapper.readValue(responseStr, ReplaceDSResponseMessage.class);                    
                    String provStepMessage = mapper.writeValueAsString(provMessageStep);
                    log.info(provStepMessage);
                    mq.sendProvMessage(provStepMessage);
                                                      
                    String provWorkflowMessage2 = mapper.writeValueAsString(getWorkflowExecutedMessage(c));
                    log.info(provWorkflowMessage2);
                    mq.sendProvMessage(provWorkflowMessage2);

                    String provWorkflowMessage = mapper.writeValueAsString(getWorkflowDescribeMessage(c));
                    log.info(provWorkflowMessage);
                    mq.sendProvMessage(provWorkflowMessage);

                    
                } else if (fileEntries.size() > 0) {
                    throw new Exception("File inputs are not implemented yet!");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                stepProv.getActivity().setStatus("FAILED");
                String provStepMessage = mapper.writeValueAsString(provMessageStep);
                log.info(provStepMessage);
                mq.sendProvMessage(mapper.writeValueAsString(provMessageStep));
                ContextUtils.sendError(ctx, "Error occured.", ex, ex.getMessage());

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if(c != null) {
                try {
                    c.close();
                } catch (RepositoryException ex) {
                    java.util.logging.Logger.getLogger(ReplaceDS.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if(mq != null) {
                try {
                    mq.close();
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(ReplaceDS.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private String getSinglePropertyValue(RepositoryConnection c, URI graph, URI prop) throws Exception {
        RepositoryResult<Statement> r = c.getStatements(null, prop, null, false, graph);
        if (r.hasNext()) {
            Statement stmt = r.next();
            return stmt.getObject().stringValue();
        } else {
            return null;
        }

    }

    private void writeGraph(RepositoryConnection conn, URI graph, OutputStream out) throws Exception {

        final RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, out);
        writer.startRDF();
        System.out.println("Graph:" + graph.toString());
        RepositoryResult<Statement> r = conn.getStatements(null, null, null, false, graph);
        if (r.hasNext()) {
            Statement stmt = null;
            while ((stmt = r.next()) != null) {
                writer.handleStatement(stmt);
                if (!r.hasNext()) {
                    break;
                }
            }

        }
        writer.endRDF();
    }

    private Provenance getStepProv() throws Exception {

        Provenance provContent = new Provenance();
        
        Agent provAgent = new Agent();
        provAgent.setID("UnifiedViews");
        provAgent.setRole("ETL");

        Activity provAct = new Activity();
        provAct.setTitle("Replace content of an existing dataset");
        provAct.setType("StepExecution");

        Communication provCom = new Communication();
        provCom.setRole("storage");
        provCom.setAgent("GraphManager");
        provCom.setInput(new ArrayList<DataProperty>());
        //DataProperty comInput = new DataProperty();
        //comInput.setKey("graphManagerInput");
        //comInput.setRole("Dataset");
        //provCom.getInput().add(comInput);
        
        provAct.setCommunication(new ArrayList<Communication>());
        provAct.getCommunication().add(provCom);

        
        provContent.setContext(getProvenanceContext());
        provContent.setAgent(provAgent);
        provContent.setActivity(provAct);
        return provContent;
    }
}
