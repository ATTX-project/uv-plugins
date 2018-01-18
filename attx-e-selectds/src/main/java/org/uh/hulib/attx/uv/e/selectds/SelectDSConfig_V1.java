package org.uh.hulib.attx.uv.e.selectds;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration class for SelectExistingDataSets.
 *
 * @author Unknown
 */
public class SelectDSConfig_V1 {

    private List<OptionValue> inputGraphs = new ArrayList<OptionValue>();

    public List<OptionValue> getInputGraphs() {
        return inputGraphs;
    }

    public void setInputGraphs(List<OptionValue> inputGraphs) {
        this.inputGraphs = inputGraphs;
    }

        
    public SelectDSConfig_V1() {

    }

}
