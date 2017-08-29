package org.uh.hulib.attx.wc.uv.t.describeexternalds;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
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
import java.util.ArrayList;
import java.util.List;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.DC;
import org.openrdf.model.vocabulary.RDF;

/**
 * Main data processing unit class.
 *
 * @author Unknown
 */
@DPU.AsTransformer
public class DescribeExternalDS extends AbstractDpu<DescribeExternalDSConfig_V1> {

    private static final Logger log = LoggerFactory.getLogger(DescribeExternalDS.class);

    
    @DataUnit.AsOutput(name = "metadata")
    public WritableRDFDataUnit outRdfData;

    @ExtensionInitializer.Init(param = "outRdfData")
    public WritableSimpleRdf rdfData;
        
	public DescribeExternalDS() {
		super(DescribeExternalDSVaadinDialog.class, ConfigHistory.noHistory(DescribeExternalDSConfig_V1.class));
	}
		
    @Override
    protected void innerExecute() throws DPUException {
        System.out.println("DescribeExternalDataSource");
        ContextUtils.sendShortInfo(ctx, "DescribeExternalDataSource.message");
        try {
            final ValueFactory valueFactory = rdfData.getValueFactory();
            // output graph rdfData -> entry -> outRdfData
            final RDFDataUnit.Entry entry = RdfDataUnitUtils.addGraph(outRdfData, 
                    DataUnitUtils.generateSymbolicName(DescribeExternalDS.class));

            final EntityBuilder targetDataset = new EntityBuilder(valueFactory.createURI(getDataSetURI()),                    
                valueFactory);
            
            targetDataset.property(DC.TITLE, config.getTitle());
            targetDataset.property(DC.RIGHTS, config.getLicence());
            if(config.getDescription() != null) {
                targetDataset.property(DC.DESCRIPTION, config.getDescription());
            }
                
            
            
            
            rdfData.setOutput(entry);
            rdfData.add(targetDataset.asStatements());
            
            ContextUtils.sendShortInfo(ctx, "Data set metadata created");
            
        }catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    private String getDataSetURI() {
        String uri = "http://data.hulib.helsinki.fi/attx/";
        long pipelineID = ctx.getExecMasterContext().getDpuContext().getPipelineId();
        long dpuInstanceID = ctx.getExecMasterContext().getDpuContext().getDpuInstanceId();
        long executionID = ctx.getExecMasterContext().getDpuContext().getPipelineExecutionId();
                
        return uri + "work/wf_" + pipelineID + "/dpu_" + dpuInstanceID + "/exec_" + executionID;
    }

}
