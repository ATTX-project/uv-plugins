/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uh.hulib.attx.wc.uv.t.retrieveds;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.DataUnitUtils;
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
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.DC;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.uh.hulib.attx.wc.uv.common.RabbitMQClient;
import org.uh.hulib.attx.wc.uv.common.pojos.ConstructRequestMessage;
import org.uh.hulib.attx.wc.uv.common.pojos.ConstructResponseMessage;
import org.uh.hulib.attx.wc.uv.common.pojos.GraphManagerQueryInput;
import org.uh.hulib.attx.wc.uv.common.pojos.GraphManagerRetrieveInput;
import org.uh.hulib.attx.wc.uv.common.pojos.ProvenanceMessage;
import org.uh.hulib.attx.wc.uv.common.pojos.RetrieveDSRequestMessage;
import org.uh.hulib.attx.wc.uv.common.pojos.RetriveDSResponseMessage;
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
public class RetrieveDS extends AbstractDpu<RetrieveDSConfig_V1> {

    public static final String datasetURISymbolicName = "datasetURI";

    private static final Logger log = LoggerFactory.getLogger(RetrieveDS.class);

    private long workflowID;
    private long executionID;
    private long stepID;

    @DataUnit.AsInput(name = "datasetURIs", optional=false)
    public RDFDataUnit datasetURIs;

    @DataUnit.AsOutput(name = "fileURI", optional=false)
    public WritableRDFDataUnit fileURI;

    @ExtensionInitializer.Init(param = "fileURI")
    public WritableSimpleRdf rdfData;

    public RetrieveDS() {
        super(RetrieveDSVaadinDialog.class, ConfigHistory.noHistory(RetrieveDSConfig_V1.class));
    }

    private Context getProvenanceContext() throws Exception {
        Context ctx = new Context();
        ctx.setActivityID("" + executionID);
        ctx.setStepID("" + stepID);
        ctx.setWorkflowID("" + workflowID);

        return ctx;
    }

    private void writeGraph(RepositoryConnection conn, org.openrdf.model.URI graph, OutputStream out) throws Exception {

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
    @Override
    protected void innerExecute() throws DPUException {

        ContextUtils.sendShortInfo(ctx, "Construct.message");
        this.stepID = ctx.getExecMasterContext().getDpuContext().getDpuInstanceId();
        this.workflowID = ctx.getExecMasterContext().getDpuContext().getPipelineId();
        this.executionID = ctx.getExecMasterContext().getDpuContext().getPipelineExecutionId();
        RepositoryConnection c = null;
        RabbitMQClient mq = null;

        try {             
            org.openrdf.model.URI[] dataSetURIEntries = new org.openrdf.model.URI[0];
            c = datasetURIs.getConnection();
            dataSetURIEntries = RDFHelper.getGraphsURIArray(datasetURIs);
            
            if (dataSetURIEntries.length > 0) {
                System.out.println("datasetURIs: " + dataSetURIEntries.length);

                mq = new RabbitMQClient("messagebroker", System.getenv("MUSER"), System.getenv("MPASS"), "provenance.inbox");
                ObjectMapper mapper = new ObjectMapper();
                Provenance prov = getProv();
                List<String> sourceGraphs = new ArrayList<String>();
                try {
                    log.info("Read data inputs");
                    for (org.openrdf.model.URI graphURI : dataSetURIEntries) {
                        writeGraph(c, graphURI, System.out);
                        List<String> inputURIs = getAllPropertyValues(c, graphURI, DC.IDENTIFIER);
                        for(String inputURI : inputURIs) {
                            log.info("Adding source: " + inputURI);
                            sourceGraphs.add(inputURI);
                            
                        }
                    }
                    log.info("Read dataset uri inputs");
                    RetrieveDSRequestMessage request = new RetrieveDSRequestMessage();
                    Provenance requestProv = new Provenance();
                    requestProv.setContext(getProvenanceContext());
                    request.setProvenance(requestProv);
                    GraphManagerRetrieveInput requestInput = new GraphManagerRetrieveInput();
                    requestInput.setOutputContentType("text/turtle");
                    requestInput.setOutputType("URI");
                    requestInput.setSourceGraphs(sourceGraphs);
                    
                    RetrieveDSRequestMessage.ReplaceDSRequestPayload p = request.new ReplaceDSRequestPayload();                    
                    p.setGraphManagerInput(requestInput);
                    request.setPayload(p);
                    
                    
                    String requestStr = mapper.writeValueAsString(request);
                    log.info(requestStr);
                    String responseText = mq.sendSyncServiceMessage(requestStr, "attx.graphManager.inbox", 10000);
                    if (responseText == null) {
                        throw new Exception("No response from service!");
                    }

                    log.info(responseText);
                    RetriveDSResponseMessage response = mapper.readValue(responseText, RetriveDSResponseMessage.class);
                    if (!response.getPayload().getStatus().equalsIgnoreCase("success")) {
                        throw new Exception("Retrieve DS failed. " + response.getPayload().getStatusMessage());
                    }
                    prov.getActivity().setStatus("success");

                    ProvenanceMessage provMsg = new ProvenanceMessage();
                    provMsg.setProvenance(prov);
                    mq.sendProvMessage(mapper.writeValueAsString(provMsg));

                    final ValueFactory vf = rdfData.getValueFactory();
                    // entry is the graph 
                    final RDFDataUnit.Entry entry = RdfDataUnitUtils.addGraph(fileURI,
                            DataUnitUtils.generateSymbolicName(RetrieveDS.class));

                    rdfData.setOutput(entry);
                    final EntityBuilder datasetUriEntity = new EntityBuilder(vf.createURI("http://hulib.helsinki.fi/attx/uv/dpu/graphManager"), vf);
                    
                    String outputFile = response.getPayload().getGraphManagerOutput();
                    if(!outputFile.startsWith("file://")) {
                        outputFile = "file://" + outputFile;
                    }
                    String contentType = "text/turtle";
                    datasetUriEntity.property(vf.createURI("http://hulib.helsinki.fi/attx/uv/dpu/fileURI"), vf.createURI(outputFile));
                    datasetUriEntity.property(vf.createURI("http://hulib.helsinki.fi/attx/uv/dpu/fileContentType"), vf.createLiteral(contentType));

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
                            java.util.logging.Logger.getLogger(RetrieveDS.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (mq != null) {
                        try {
                            mq.close();
                        } catch (IOException ex) {
                            java.util.logging.Logger.getLogger(RetrieveDS.class.getName()).log(Level.SEVERE, null, ex);
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
        provAct.setTitle("Retrieve DS");
        provAct.setType("StepExecution");

        Communication provCom = new Communication();
        provCom.setRole("storage");
        provCom.setAgent("GraphManager");
        provCom.setInput(new ArrayList<DataProperty>());

        provAct.setCommunication(new ArrayList<Communication>());
        provAct.getCommunication().add(provCom);

        DataProperty provInput = new DataProperty();
        provInput.setKey("graphManagerInput");
        provInput.setRole("Dataset");
        DataProperty provOutput = new DataProperty();
        provOutput.setKey("graphManagerOutput");
        provOutput.setRole("Dataset");

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
