package org.uh.hulib.attx.uv.e.selectds;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.DataUnitUtils;
import eu.unifiedviews.helpers.dataunit.rdf.RdfDataUnitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.rdf.simple.WritableSimpleRdf;
import eu.unifiedviews.helpers.dpu.rdf.EntityBuilder;
import java.io.OutputStream;
import java.util.List;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.DC;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

/**
 * Main data processing unit class.
 *
 * @author Unknown
 */
@DPU.AsExtractor
public class SelectDS extends AbstractDpu<SelectDSConfig_V1> {

    private static final Logger log = LoggerFactory.getLogger(SelectDS.class);

    @DataUnit.AsOutput(name = "datasetURIs")
    public WritableRDFDataUnit outRdfData;

    @ExtensionInitializer.Init(param = "outRdfData")
    public WritableSimpleRdf rdfData;

    
	public SelectDS() {
		super(SelectDSVaadinDialog.class, ConfigHistory.noHistory(SelectDSConfig_V1.class));
	}
		
    @Override
    protected void innerExecute() throws DPUException {

        ContextUtils.sendShortInfo(ctx, "SelectExistingDataSets.message");
        try {
            List<OptionValue> values = config.getInputGraphs();
            log.info("***: " + values.size());
            
            final ValueFactory valueFactory = rdfData.getValueFactory();
            // output graph rdfData -> entry -> outRdfData
            final RDFDataUnit.Entry entry = RdfDataUnitUtils.addGraph(outRdfData, 
                    DataUnitUtils.generateSymbolicName(SelectDS.class));
            rdfData.setOutput(entry);

            for(OptionValue v : values) {
                log.info("Adding graph: "+ v.getValue());
                final EntityBuilder targetDataset = new EntityBuilder(valueFactory.createURI(v.getValue()),                    
                    valueFactory);
                targetDataset.property(DC.IDENTIFIER, valueFactory.createURI(v.getValue()));
                
                rdfData.add(targetDataset.asStatements());
            }
            rdfData.flushBuffer();
            writeGraph(outRdfData.getConnection(), entry.getDataGraphURI(), System.out);
            ContextUtils.sendShortInfo(ctx, "Data set metadata created");
            
        }catch(Exception ex) {
            ex.printStackTrace();
            ContextUtils.sendError(ctx, "Selection failed", ex, ex.getMessage());
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
	
    private String getDataSetURI() {
        String uri = "http://data.hulib.helsinki.fi/attx/";
        long pipelineID = ctx.getExecMasterContext().getDpuContext().getPipelineId();
        long dpuInstanceID = ctx.getExecMasterContext().getDpuContext().getDpuInstanceId();
        long executionID = ctx.getExecMasterContext().getDpuContext().getPipelineExecutionId();
                
        return uri + "work/wf_" + pipelineID + "/dpu_" + dpuInstanceID + "/exec_" + executionID;
    }
}
