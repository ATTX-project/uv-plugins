/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uh.hulib.attx.wc.uv.common.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jkesanie
 */
public class GraphManagerQueryInput {
 
    public GraphManagerQueryInput() {}
    
    @JsonProperty("activity")
    private String activity;
    @JsonProperty("contentType")
    private String contentType;
    @JsonProperty("outputType")
    private String outputType;
    @JsonProperty("sourceGraphs")
    private List<String> sourceGraphs = new ArrayList<String>();
    @JsonProperty("input")
    private String input;
    
    @JsonProperty("activity")
    public String getActivity() {
        return activity;
    }

    @JsonProperty("activity")
    public void setActivity(String activity) {
        this.activity = activity;
    }
    
   @JsonProperty("contentType")
    public String getContentType() {
        return contentType;
    }

    @JsonProperty("contentType")
    public void setContentType(String contentType) {
        this.contentType = contentType;
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
    
   @JsonProperty("input")
    public String getInput() {
        return input;
    }

    @JsonProperty("input")
    public void setInput(String input) {
        this.input = input;
    }     
}
