/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uh.hulib.attx.wc.uv.common.pojos;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphManagerRetrieveInput {

    public GraphManagerRetrieveInput() {
    }

    @JsonProperty("task")
    private String task = "retrieve";
    @JsonProperty("outputContentType")
    private String outputContentType;
    @JsonProperty("outputType")
    private String outputType;
    @JsonProperty("sourceGraphs")
    private List<String> sourceGraphs = null;

    @JsonProperty("outputContentType")
    public String getOutputContentType() {
        return outputContentType;
    }

    @JsonProperty("outputContentType")
    public void setOutputContentType(String outputContentType) {
        this.outputContentType = outputContentType;
    }

    @JsonProperty("outputType")
    public String getOutputType() {
        return outputType;
    }

    @JsonProperty("outputType")
    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    @JsonProperty("sourceGraphs")
    public List<String> getSourceGraphs() {
        return sourceGraphs;
    }

    @JsonProperty("sourceGraphs")
    public void setSourceGraphs(List<String> sourceGraphs) {
        this.sourceGraphs = sourceGraphs;
    }


    
}
