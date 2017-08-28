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
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "sourceURIs",
    "targetURI"
})
public class ReplaceDSRequest {

    private List<String> sourceURIs = null;
    @JsonProperty("targetURI")
    private String targetURI;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("sourceURIs")
    public List<String> getSourceURIs() {
        return sourceURIs;
    }

    @JsonProperty("sourceURIs")
    public void setSourceURIs(List<String> sourceURIs) {
        this.sourceURIs = sourceURIs;
    }

    @JsonProperty("targetURI")
    public String getTargetURI() {
        return targetURI;
    }

    @JsonProperty("targetURI")
    public void setTargetURI(String targetURI) {
        this.targetURI = targetURI;
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
