/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uh.hulib.attx.wc.uv.common.pojos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

/**
 *
 * @author jkesanie
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "activity",
    "targetAlias",
    "useBulk",
    "sourceData"
})
public class IndexServiceInput {

    @JsonProperty("targetAlias")
    private List<String> targetAlias = null;
    @JsonProperty("sourceData")
    private List<Source> sourceData = null;

    @JsonProperty("targetAlias")
    public List<String> getTargetAlias() {
        return targetAlias;
    }

    @JsonProperty("targetAlias")
    public void setTargetAlias(List<String> targetAlias) {
        this.targetAlias = targetAlias;
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
