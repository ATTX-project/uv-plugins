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
public class IndexServiceInput {

    @JsonProperty("task")
    private String task;
    @JsonProperty("targetAlias")
    private List<String> targetAlias = null;
    @JsonProperty("sourceData")
    private List<IndexSource> sourceData = null;

    @JsonProperty("task")
    public String getTask() {
        return task;
    }

    @JsonProperty("task")
    public void setTask(String task) {
        this.task = task;
    }
    
    @JsonProperty("targetAlias")
    public List<String> getTargetAlias() {
        return targetAlias;
    }

    @JsonProperty("targetAlias")
    public void setTargetAlias(List<String> targetAlias) {
        this.targetAlias = targetAlias;
    }

    @JsonProperty("sourceData")
    public List<IndexSource> getSourceData() {
        return sourceData;
    }

    @JsonProperty("sourceData")
    public void setSourceData(List<IndexSource> sourceData) {
        this.sourceData = sourceData;
    }

}
