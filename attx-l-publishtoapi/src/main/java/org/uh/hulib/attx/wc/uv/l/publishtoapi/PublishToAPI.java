package org.uh.hulib.attx.wc.uv.l.publishtoapi;

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
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
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

    private String getDataSetURI() {
        String uri = "http://data.hulib.helsinki.fi/attx/";
        long pipelineID = ctx.getExecMasterContext().getDpuContext().getPipelineId();
        long dpuInstanceID = ctx.getExecMasterContext().getDpuContext().getDpuInstanceId();
        long executionID = ctx.getExecMasterContext().getDpuContext().getPipelineExecutionId();

        return uri + "work/wf_" + pipelineID + "/dpu_" + dpuInstanceID + "/exec_" + executionID;
    }

    private String getDataSetParentURI() {
        String uri = "http://data.hulib.helsinki.fi/attx/";

        long pipelineID = ctx.getExecMasterContext().getDpuContext().getPipelineId();
        long dpuInstanceID = ctx.getExecMasterContext().getDpuContext().getDpuInstanceId();

        return uri + "work/wf_" + pipelineID + "/dpu_" + dpuInstanceID;
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
        provAgent.setID("UV");
        provAgent.setRole("ETL");

        Activity provAct = new Activity();
        provAct.setTitle("Index to API");
        provAct.setType("StepExecution");

        Communication provCom = new Communication();
        provCom.setRole("indexservice");
        provCom.setAgent("indexservice");
        provCom.setInput(new ArrayList<DataProperty>());
        DataProperty comInput = new DataProperty();
        comInput.setKey("indexingServiceInput");
        comInput.setRole("unknown");
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
