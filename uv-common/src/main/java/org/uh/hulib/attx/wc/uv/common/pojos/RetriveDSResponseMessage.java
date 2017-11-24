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
public class RetriveDSResponseMessage extends AbstractMessage {
     @JsonProperty("payload")
    private RetriveDSResponseMessage.RetrieveDSResponseMessagePayload payload;

    @JsonProperty("payload")
    public RetriveDSResponseMessage.RetrieveDSResponseMessagePayload getPayload() {
        return this.payload;
    }

    @JsonProperty("payload")
    public void setPayload(RetriveDSResponseMessage.RetrieveDSResponseMessagePayload payload) {
       this.payload = payload;
    } 

    public class RetrieveDSResponseMessagePayload extends BasicPayload {

        public RetrieveDSResponseMessagePayload() {
        }
        
        @JsonProperty("graphManagerOutput")
        private String graphManagerOutput;

        @JsonProperty("graphManagerOutput")
        public String getGraphManagerOutput() {
            return graphManagerOutput;
        }

        @JsonProperty("graphManagerOutput")
        public void setGraphManagerOutput(String graphManagerOutput) {
            this.graphManagerOutput = graphManagerOutput;
        }
    }    
}
