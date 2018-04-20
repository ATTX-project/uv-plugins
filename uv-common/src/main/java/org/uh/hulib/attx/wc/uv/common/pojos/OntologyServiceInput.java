/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uh.hulib.attx.wc.uv.common.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 *
 * @author jkesanie
 */
public class OntologyServiceInput {
    @JsonProperty("activity")
    private String activity;
    @JsonProperty("sourceData")
    private List<Source> sourceData = null;

    @JsonProperty("activity")
    public String getActivity() {
        return activity;
    }

    @JsonProperty("activity")
    public void setActivity(String activity) {
        this.activity = activity;
    }

    @JsonProperty("sourceData")
    public List<Source> getSourceData() {
        return sourceData;
    }

    @JsonProperty("sourceData")
    public void setSourceData(List<Source> sourceData) {
        this.sourceData = sourceData;
    }
    
}
