/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uh.hulib.attx.wc.uv.common.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author jkesanie
 */
public class ConstructResponseMessage extends AbstractMessage {
     @JsonProperty("payload")
    private ConstructResponseMessage.ConstructResponseMessagePayload payload;

    @JsonProperty("payload")
    public ConstructResponseMessage.ConstructResponseMessagePayload getPayload() {
        return this.payload;
    }

    @JsonProperty("payload")
    public void setPayload(ConstructResponseMessage.ConstructResponseMessagePayload payload) {
       this.payload = payload;
    } 

    public class ConstructResponseMessagePayload extends BasicPayload {

        public ConstructResponseMessagePayload() {
        }
        
        @JsonProperty("graphManagerOutput")
        private String graphManagerConstructOutput;

        @JsonProperty("graphManagerOutput")
        public String getGraphManagerOutput() {
            return graphManagerConstructOutput;
        }

        @JsonProperty("graphManagerOutput")
        public void setGraphManagerOutput(String graphManagerConstructOutput) {
            this.graphManagerConstructOutput = graphManagerConstructOutput;
        }
    }    
}
