/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uh.hulib.attx.wc.uv.common.pojos;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "sourceURI",
    "sourceData",
    "mapping"
})
public class RMLServiceRequest {

    @JsonProperty("sourceURI")
    private String sourceURI;
    @JsonProperty("sourceData")
    private String sourceData;
    @JsonProperty("mapping")
    private String mapping;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("sourceURI")
    public String getSourceURI() {
        return sourceURI;
    }

    @JsonProperty("sourceURI")
    public void setSourceURI(String sourceURI) {
        this.sourceURI = sourceURI;
    }

    @JsonProperty("sourceData")
    public String getSourceData() {
        return sourceData;
    }

    @JsonProperty("sourceData")
    public void setSourceData(String sourceData) {
        this.sourceData = sourceData;
    }

    @JsonProperty("mapping")
    public String getMapping() {
        return mapping;
    }

    @JsonProperty("mapping")
    public void setMapping(String mapping) {
        this.mapping = mapping;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
