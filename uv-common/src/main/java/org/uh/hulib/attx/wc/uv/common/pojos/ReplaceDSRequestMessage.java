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

public class ReplaceDSRequestMessage extends AbstractMessage {

    @JsonProperty("payload")
    private ReplaceDSRequestPayload payload;
    
    @JsonProperty("payload")
    public ReplaceDSRequestPayload getPayload() {
        return this.payload;
    }

    @JsonProperty("payload")
    public void setPayload(ReplaceDSRequestPayload payload) {
        this.payload = payload;
    }    
    
    
    public class ReplaceDSRequestPayload extends BasicPayload {

        public ReplaceDSRequestPayload() {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonPropertyOrder({
            "graphManagerInput"
        })

        @JsonProperty("graphManagerInput")
        private GraphManagerInput graphManagerInput;

        @JsonProperty("graphManagerInput")
        public GraphManagerInput getGraphManagerInput() {
            return graphManagerInput;
        }

        @JsonProperty("graphManagerInput")
        public void setGraphManagerInput(GraphManagerInput graphManagerInput) {
            this.graphManagerInput = graphManagerInput;
        }
    }

    

}
