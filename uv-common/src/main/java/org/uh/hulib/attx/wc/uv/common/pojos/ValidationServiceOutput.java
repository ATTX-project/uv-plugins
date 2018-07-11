package org.uh.hulib.attx.wc.uv.common.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ValidationServiceOutput {
    @JsonProperty("contentType")
    private String contentType;
    @JsonProperty("outputType")
    private String outputType;
    @JsonProperty("output")
    private String output;

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
    public String getOutput() {
        return output;
    }

    @JsonProperty("output")
    public void setOutput(String output) {
        this.output = output;
    }
}
