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
import java.util.Map;

    public class GraphManagerInput {

        public GraphManagerInput() {}
        
        @JsonProperty("contentType")
        private String contentType;
        @JsonProperty("inputType")
        private String inputType;
        @JsonProperty("namedGraph")
        private String namedGraph;
        @JsonProperty("input")
        private String input;
        @JsonProperty("activity")
        private String activity;
        @JsonIgnore
        private Map<String, Object> additionalProperties = new HashMap<String, Object>();

        @JsonProperty("contentType")
        public String getContentType() {
            return contentType;
        }

        @JsonProperty("contentType")
        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        @JsonProperty("inputType")
        public String getInputType() {
            return inputType;
        }

        @JsonProperty("inputType")
        public void setInputType(String inputType) {
            this.inputType = inputType;
        }

        @JsonProperty("namedGraph")
        public String getNamedGraph() {
            return namedGraph;
        }

        @JsonProperty("namedGraph")
        public void setNamedGraph(String namedGraph) {
            this.namedGraph = namedGraph;
        }

        @JsonProperty("input")
        public String getInput() {
            return input;
        }

        @JsonProperty("input")
        public void setInput(String input) {
            this.input = input;
        }

        @JsonProperty("activity")
        public String getActivity() {
            return activity;
        }

        @JsonProperty("activity")
        public void setActivity(String activity) {
            this.activity = activity;
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
