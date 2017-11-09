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

/**
 *
 * @author jkesanie
 */
public class RMLServiceInput {
        @JsonProperty("mapping")
        private String mapping;
        @JsonProperty("sourceData")
        private List<Source> sourceData = null;
        
        @JsonIgnore
        private Map<String, Object> additionalProperties = new HashMap<String, Object>();

        @JsonProperty("mapping")
        public String getMapping() {
            return mapping;
        }

        @JsonProperty("mapping")
        public void setMapping(String mapping) {
            this.mapping = mapping;
        }
        @JsonProperty("sourceData")
        public List<Source> getSourceData() {
            return sourceData;
        }

        @JsonProperty("sourceData")
        public void setSourceData(List<Source> sourceData) {
            this.sourceData = sourceData;
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
