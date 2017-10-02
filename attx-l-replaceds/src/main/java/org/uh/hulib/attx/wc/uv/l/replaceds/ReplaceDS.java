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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.DC;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.uh.hulib.attx.wc.uv.common.MessagingClient;
import org.uh.hulib.attx.wc.uv.common.RabbitMQClient;
import org.uh.hulib.attx.wc.uv.common.pojos.GraphManagerInput;
import org.uh.hulib.attx.wc.uv.common.pojos.ProvenanceMessage;
import org.uh.hulib.attx.wc.uv.common.pojos.ReplaceDSRequest;
import org.uh.hulib.attx.wc.uv.common.pojos.ReplaceDSResponse;
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
        return "http://data.hulib.helsinki.fi/attx/work/wf_" + workflowID + "_step_" + stepID + "/input"; 
    }
    
    private String getInputGraphURI() throws Exception {
        return "http://data.hulib.helsinki.fi/attx/work/wf_" + workflowID + "_step_" + stepID + "/output"; 
    }
    
    private Context getProvenanceContext() throws Exception {
        Context ctx = new Context();
        ctx.setActivityID("" +executionID);
        ctx.setStepID("" + stepID);
        ctx.setWorkflowID("" + workflowID);
                
        return ctx;
    }  
    
    private ProvenanceMessage getWorkflowExecutionMessage() throws Exception {
        ProvenanceMessage provMessage = new ProvenanceMessage();
        // outputDatasetMetadata is required 
        RepositoryConnection c = outputDatasetMetadata.getConnection();


        Map<String, Object> payload = new HashMap<String, Object>();

        Provenance prov = new Provenance();
        Agent provAgent = new Agent();
        provAgent.setID("UV");
        provAgent.setRole("ETL");
        prov.setAgent(provAgent);

        Activity provAct = new Activity();
        provAct.setTitle("TODO: name of the workflow should be here");
        provAct.setType("DescribeStepExecution");
        prov.setActivity(provAct);

        URI[] datasetMetadataGraphs = RDFHelper.getGraphsURIArray(outputDatasetMetadata);
        
        
        if (datasetMetadataGraphs.length > 0) {
            // just using the first one for now!
            URI g = datasetMetadataGraphs[0];
            
            writeGraph(c, g, System.out);
            
            String targetGraph = getOutputGraphURI();
            String targetGraphTitle = getSinglePropertyValue(c, g, DC.TITLE);
            String targetGraphDesc = getSinglePropertyValue(c, g, DC.DESCRIPTION);

            DataProperty output = new DataProperty();
            output.setKey("inputDataset");
            output.setRole("Dataset");
            prov.setOutput(new ArrayList<DataProperty>());
            prov.getOutput().add(output);

            Map<String, Object> outputDataset = new HashMap<String, Object>();
            outputDataset.put("uri", targetGraph);
            outputDataset.put("title", targetGraphTitle);
            outputDataset.put("description", targetGraphDesc);

            payload.put("inputDataset", outputDataset);
        }

        URI[] externalDatasetMetadataGraphs = RDFHelper.getGraphsURIArray(inputDatasetMetadata);
        if (externalDatasetMetadataGraphs.length > 0) {
            // just using the first one for now!
            URI g = externalDatasetMetadataGraphs[0];
            
            writeGraph(c, g, System.out);
            
            
            String sourceGraph = getInputGraphURI();
            String sourceGraphTitle = getSinglePropertyValue(c, g, DC.TITLE);
            String sourceGraphDesc = getSinglePropertyValue(c, g, DC.DESCRIPTION);
            String sourceGraphPublisher = getSinglePropertyValue(c, g, DC.PUBLISHER);
            String sourceGraphLicense = getSinglePropertyValue(c, g, DC.RIGHTS);

            DataProperty input = new DataProperty();
            input.setKey("outputDataset");
            input.setRole("Dataset");
            prov.setInput(new ArrayList<DataProperty>());
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

            payload.put("outputDataset", inputDataset);

        }
        // check for existing datasets         
        
        provMessage.setProvenance(prov);
        provMessage.setPayload(payload);

        return provMessage;

    }

    @Override
    protected void innerExecute() throws DPUException {

        ContextUtils.sendShortInfo(ctx, "ReplaceDS.message");
        this.stepID = ctx.getExecMasterContext().getDpuContext().getDpuInstanceId();
        this.workflowID = ctx.getExecMasterContext().getDpuContext().getPipelineId();
        this.executionID = ctx.getExecMasterContext().getDpuContext().getPipelineExecutionId();


        try {
            MessagingClient mq = new RabbitMQClient("messagebroker", System.getenv("RABBITMQ_DEFAULT_USER"), System.getenv("RABBITMQ_DEFAULT_PASS"), "provenance.inbox");
            ObjectMapper mapper = new ObjectMapper();
            ProvenanceMessage provMessageStep = new ProvenanceMessage();            
            Provenance stepProv = getStepProv();
            provMessageStep.setProvenance(stepProv);
            
            try {

                RepositoryConnection c = outputDatasetMetadata.getConnection();

                // create dataset related prov content
                

                Set<FilesDataUnit.Entry> fileEntries = FilesHelper.getFiles(dataFiles);
                System.out.println("datasetFiles size:" + fileEntries.size());

                URI[] uriEntries = RDFHelper.getGraphsURIArray(dataURIs);
                System.out.println("dataSetURIs size:" + uriEntries.length);

                if (uriEntries.length > 0) {
                    List<String> inputURIs = new ArrayList<>();

                    for (URI graphURI : uriEntries) {
                        writeGraph(c, graphURI, System.out);

                        String inputURI = getSinglePropertyValue(c, graphURI, c.getValueFactory().createURI("http://hulib.helsinki.fi/attx/uv/dpu/fileURI"));
                        if (inputURI != null) {
                            inputURIs.add(inputURI);
                        }
                    }
                    ReplaceDSRequest request = new ReplaceDSRequest();
                    ReplaceDSRequest.ReplaceDSRequestPayload p = request.new ReplaceDSRequestPayload();

                    
                    GraphManagerInput graphManagerInput = new GraphManagerInput();
                    graphManagerInput.setActivity("replace");
                    graphManagerInput.setContentType("application/n-triples");
                    graphManagerInput.setInputType("URI");
                    graphManagerInput.setNamedGraph(getOutputGraphURI());
                    graphManagerInput.setInput(inputURIs.get(0));
                    
                    p.setGraphManagerInput(graphManagerInput);
                    request.setPayload(p);
                    Provenance requestProv = new Provenance();
                    requestProv.setContext(getProvenanceContext());
                    request.setProvenance(requestProv);
                    
                    String requestStr = mapper.writeValueAsString(request);
                    String responseStr = mq.sendSyncServiceMessage(requestStr, "attx.graphManager.inbox", 10000);
                    
                    System.out.println(responseStr);
                    if(responseStr == null) {
                        throw new Exception("Null response from service");                    
                    }

                    ReplaceDSResponse response = mapper.readValue(responseStr, ReplaceDSResponse.class);
                                        
                    
                    // add payload to the stepProv
                    provMessageStep.getPayload().put("transformerData", getInputGraphURI());
                    provMessageStep.getPayload().put("outputDataset", getOutputGraphURI());

                    mq.sendProvMessage(mapper.writeValueAsString(provMessageStep));
                                                           
                    mq.sendProvMessage(mapper.writeValueAsString(getWorkflowExecutionMessage()));

                    
                } else if (fileEntries.size() > 0) {
                    throw new Exception("File inputs are not implemented yet!");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                stepProv.getActivity().setStatus("FAILED");
                mq.sendProvMessage(mapper.writeValueAsString(provMessageStep));
                ContextUtils.sendError(ctx, "Error occured.", ex, ex.getMessage());

            }

        } catch (Exception ex) {
            ex.printStackTrace();
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
        provAgent.setID("UV");
        provAgent.setRole("ETL");

        Activity provAct = new Activity();
        provAct.setTitle("Replace content of an existing dataset");
        provAct.setType("StepExecution");

        Communication provCom = new Communication();
        provCom.setRole("graphmanager");
        provCom.setAgent("GMAPI");
        provCom.setInput(new ArrayList<DataProperty>());
        DataProperty comInput = new DataProperty();
        comInput.setKey("transformerData");
        comInput.setRole("tempDataset");
        provCom.getInput().add(comInput);
        
        provAct.setCommunication(new ArrayList<Communication>());
        provAct.getCommunication().add(provCom);

        DataProperty provInput = new DataProperty();
        provInput.setKey("transformerData");
        provInput.setRole("tempDataset");
        
        DataProperty provOutput = new DataProperty();
        provOutput.setKey("outputDataset");
        provOutput.setRole("Dataset");
        
        provContent.setContext(getProvenanceContext());
        provContent.setAgent(provAgent);
        provContent.setActivity(provAct);
        provContent.setInput(new ArrayList());
        provContent.getInput().add(provInput);
        provContent.setOutput(new ArrayList());
        provContent.getOutput().add(provOutput);
        return provContent;
    }
}
