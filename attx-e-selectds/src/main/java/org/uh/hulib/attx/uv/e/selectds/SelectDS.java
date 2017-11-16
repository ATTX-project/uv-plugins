package org.uh.hulib.attx.uv.e.selectds;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;

/**
 * Main data processing unit class.
 *
 * @author Unknown
 */
@DPU.AsExtractor
public class SelectDS extends AbstractDpu<SelectDSConfig_V1> {

    private static final Logger log = LoggerFactory.getLogger(SelectDS.class);

    @DataUnit.AsOutput(name = "Selected dataset URIs")
    public WritableRDFDataUnit outRdfData;

    
	public SelectDS() {
		super(SelectDSVaadinDialog.class, ConfigHistory.noHistory(SelectDSConfig_V1.class));
	}
		
    @Override
    protected void innerExecute() throws DPUException {

        ContextUtils.sendShortInfo(ctx, "SelectExistingDataSets.message");
        
    }
	
}
