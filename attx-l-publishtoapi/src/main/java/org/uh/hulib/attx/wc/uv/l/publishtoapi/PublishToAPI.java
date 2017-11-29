package org.uh.hulib.attx.wc.uv.l.publishtoapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
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
import org.uh.hulib.attx.wc.uv.common.RabbitMQClient;
import org.uh.hulib.attx.wc.uv.common.pojos.IndexServiceInput;
import org.uh.hulib.attx.wc.uv.common.pojos.IndexServiceRequestMessage;
import org.uh.hulib.attx.wc.uv.common.pojos.IndexServiceResponseMessage;
import org.uh.hulib.attx.wc.uv.common.pojos.IndexSource;
import org.uh.hulib.attx.wc.uv.common.pojos.ProvenanceMessage;
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
public class PublishToAPI extends AbstractDpu<PublishToAPIConfig_V1> {

    private static final Logger log = LoggerFactory.getLogger(PublishToAPI.class);
    private long workflowID;
    private long executionID;
    private long stepID;
    private ObjectMapper mapper = new ObjectMapper();

//    @DataUnit.AsInput(name = "fileInput", optional = true)
//    public FilesDataUnit fileInput;

    @DataUnit.AsInput(name = "uriInput", optional = false)
    public RDFDataUnit uriInput;
    
    @DataUnit.AsInput(name = "outputDatasetMetadata", optional = false)
    public RDFDataUnit outputDatasetMetadata;
    
    @DataUnit.AsInput(name = "inputDatasetMetadata", optional = true)
    public RDFDataUnit inputDatasetMetadata;
 
    
    public PublishToAPI() {
        super(PublishToAPIVaadinDialog.class, ConfigHistory.noHistory(PublishToAPIConfig_V1.class));
    }

    @Override
    protected void innerExecute() throws DPUException {

        ContextUtils.sendShortInfo(ctx, "PublishToAPI.message");
        this.stepID = ctx.getExecMasterContext().getDpuContext().getDpuInstanceId();
        this.workflowID = ctx.getExecMasterContext().getDpuContext().getPipelineId();
        this.executionID = ctx.getExecMasterContext().getDpuContext().getPipelineExecutionId();
        RepositoryConnection c = null;
        RabbitMQClient mq = null;
        try {
            mq = new RabbitMQClient("messagebroker", System.getenv("MUSER"), System.getenv("MPASS"), "provenance.inbox");
            c = this.uriInput.getConnection();
            
            org.openrdf.model.URI[] uriEntries = RDFHelper.getGraphsURIArray(uriInput);
            System.out.println("framerEntries size:" + uriEntries.length);
            
            if(uriEntries.length > 0) {
                IndexServiceRequestMessage req = new IndexServiceRequestMessage();
                
                IndexServiceInput input = new IndexServiceInput();
                input.setTargetAlias(new ArrayList<String>());
                input.getTargetAlias().add(config.getAlias());
                // TODO: fix this
                input.setSourceData(new ArrayList());
                
                for (org.openrdf.model.URI graphURI : uriEntries) {
                    writeGraph(c, graphURI, System.out);
                    String inputURI = getSinglePropertyValue(c, graphURI, c.getValueFactory().createURI("http://hulib.helsinki.fi/attx/uv/dpu/fileURI"));
                    String contentType = getSinglePropertyValue(c, graphURI, c.getValueFactory().createURI("http://hulib.helsinki.fi/attx/uv/dpu/fileContentType"));
                    String docType = getSinglePropertyValue(c, graphURI, c.getValueFactory().createURI("http://hulib.helsinki.fi/attx/uv/dpu/docType"));

                    IndexSource source = new IndexSource();
                    source.setInputType("URI");
                    source.setInput(inputURI);
                    source.setUseBulk(true);
                    source.setContentType(contentType);
                    source.setDocType(docType);                            
                    input.getSourceData().add(source);
                }

                input.setTask("replace");

                req.setProvenance(new Provenance());
                req.getProvenance().setContext(getProvenanceContext());
                
                IndexServiceRequestMessage.IndexServiceRequestPayload p = req.new IndexServiceRequestPayload();
                p.setGraphManagerInput(input);
                req.setPayload(p);
                
                String requestStr = mapper.writeValueAsString(req);
                log.info(requestStr);
                String responseStr = mq.sendSyncServiceMessage(requestStr, "attx.indexing.inbox", 10000);

                log.info(responseStr);
                if(responseStr == null) {
                    throw new Exception("Null response from the service");                    
                }
                
                IndexServiceResponseMessage response = mapper.readValue(responseStr, IndexServiceResponseMessage.class);
                
                if(response.getPayload().getStatus().equalsIgnoreCase("success")) {
                    ContextUtils.sendShortInfo(ctx, "Indexing successful");        
                    
                    ProvenanceMessage provStepMessage = new ProvenanceMessage();            
                    Provenance stepProv = getStepProv();
                    provStepMessage.setProvenance(stepProv);
                    addInputsAndOutputs(c, provStepMessage);
                    String provStepMessageStr = mapper.writeValueAsString(provStepMessage);
                    log.info(provStepMessageStr);
                    mq.sendProvMessage(provStepMessageStr);
                    
                    String provWorkflowDescMessage = mapper.writeValueAsString(getWorkflowDescribeMessage(c));
                    log.info(provWorkflowDescMessage);
                    mq.sendProvMessage(provWorkflowDescMessage);
                    
                    String provWorkflowExecMessage = mapper.writeValueAsString(getWorkflowExecutedMessage2(c));
                    log.info(provWorkflowExecMessage);
                    mq.sendProvMessage(provWorkflowExecMessage);                    

                    
                }
                else {
                    ContextUtils.sendError(ctx, "Indexing failed.", "");
                }
                
                // TODO: send prov message
                       
            }
            
            ContextUtils.sendShortInfo(ctx, "Indexing done");

        } catch (Exception ex) {
            ex.printStackTrace();
            // TODO: send ERROR prov message

        } finally {
            if (c != null) {
                try {
                    c.close();
                } catch (RepositoryException ex) {
                    java.util.logging.Logger.getLogger(PublishToAPI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (mq != null) {
                try {
                    mq.close();
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(PublishToAPI.class.getName()).log(Level.SEVERE, null, ex);
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
    
    
    private Context getProvenanceContext() throws Exception {
        Context ctx = new Context();
        ctx.setActivityID("" +executionID);
        ctx.setStepID("" + stepID);
        ctx.setWorkflowID("" + workflowID);
                
        return ctx;
    }  

    private Provenance getStepProv() throws Exception {

        Provenance provContent = new Provenance();
        
        
        Agent provAgent = new Agent();
        provAgent.setID("UnifiedViews");
        provAgent.setRole("ETL");

        Activity provAct = new Activity();
        provAct.setTitle("Index to API");
        provAct.setType("StepExecution");

        Communication provCom = new Communication();
        provCom.setRole("index");
        provCom.setAgent("IndexingService");
        provCom.setInput(new ArrayList<DataProperty>());
        DataProperty comInput = new DataProperty();
        comInput.setKey("indexingServiceInput");
        comInput.setRole("Dataset");
        provCom.getInput().add(comInput);
        
        provAct.setCommunication(new ArrayList<Communication>());
        provAct.getCommunication().add(provCom);

        provContent.setAgent(provAgent);
        provContent.setActivity(provAct);
        provContent.setContext(getProvenanceContext());

        return provContent;
    }    
    
    private String getOutputGraphURI() throws Exception {
        return "http://data.hulib.helsinki.fi/attx/work/wf_" + workflowID + "_step_" + stepID + "/output"; 
    }    
    
    private ProvenanceMessage getWorkflowExecutedMessage2(RepositoryConnection c) throws Exception {
        ProvenanceMessage provMessage = new ProvenanceMessage();
        Map<String, Object> payload = new HashMap<String, Object>();
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
        URI[] outputGraphsMetadata = RDFHelper.getGraphsURIArray(outputDatasetMetadata);
        
        
        if (outputGraphsMetadata.length > 0) {
            // just using the first one for now!
            URI g = outputGraphsMetadata[0];
            
            writeGraph(c, g, System.out);
            
            String targetGraph = getOutputGraphURI();

            DataProperty output = new DataProperty();
            output.setKey("outputDataset");
            output.setRole("Dataset");
            prov.setOutput(new ArrayList<DataProperty>());
            prov.getOutput().add(output);

            
            Map<String, Object> outputDataset = new HashMap<String, Object>();
            outputDataset.put("uri", targetGraph);

            payload.put("outputDataset", outputDataset);
        }
        
        URI[] inputGraphsMetadata = RDFHelper.getGraphsURIArray(inputDatasetMetadata);
        log.info("***: "+ inputGraphsMetadata.length);
        prov.setInput(new ArrayList<DataProperty>());        
        if (inputGraphsMetadata.length > 0) {
            for(int i = 0; i < inputGraphsMetadata.length; i++) {                
                URI g = inputGraphsMetadata[i];    
//                writeGraph(c, g, System.out);
                
                List<String> inputURIs = getAllPropertyValues(c, g, DC.IDENTIFIER);
                if(inputURIs.size() == 0) {
                    // get the inputs from the external ds step 
                    inputURIs.add(g.toString());
                }
                log.info("inputURIS:" + inputURIs.size());
                for(int i2 = 0; i2 < inputURIs.size(); i2++) {
                    String inputURI = inputURIs.get(i2);
                    log.info("inputURI:" + inputURI);

                    DataProperty input = new DataProperty();
                    input.setKey("inputDataset" + i2);
                    input.setRole("Dataset");
                    prov.getInput().add(input);

                    payload.put("inputDataset" + i2, inputURI); 
                }
            }
        }         
        
        provMessage.setProvenance(prov);
        provMessage.setPayload(payload);
        
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
        prov.getContext().setStepID("");
        
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

                if(sourceGraphTitle != null) {
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

        URI[] inputGraphsMetadata = RDFHelper.getGraphsURIArray(uriInput);
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
    
    private List<String> getAllPropertyValues(RepositoryConnection c, org.openrdf.model.URI graph, org.openrdf.model.URI prop) throws Exception {
        RepositoryResult<Statement> r = c.getStatements(null, prop, null, false, graph);
        List<String> values = new ArrayList<String>();
        while (r.hasNext()) {
            Statement stmt = r.next();
            values.add( stmt.getObject().stringValue());
        }
        return values;

    }      
}
