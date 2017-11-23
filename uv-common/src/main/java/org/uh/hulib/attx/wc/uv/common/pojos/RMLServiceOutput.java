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
public class RMLServiceOutput {
    @JsonProperty("contentType")
    private String contentType;    
    @JsonProperty("outputType")
    private String outputType;
    @JsonProperty("output")
    private List<String> output;

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

    
    @JsonProperty("output")
    public List<String> getOutput() {
        return output;
    }

    @JsonProperty("output")
    public void setOutput(List<String> output) {
        this.output = output;
    }
 
}
