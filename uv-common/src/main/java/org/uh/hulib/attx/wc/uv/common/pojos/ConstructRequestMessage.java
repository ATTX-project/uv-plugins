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
public class ConstructRequestMessage extends AbstractMessage {
     @JsonProperty("payload")
    private ConstructRequestMessage.ConstructRequestMessagePayload payload = new ConstructRequestMessage.ConstructRequestMessagePayload();

    @JsonProperty("payload")
    public ConstructRequestMessage.ConstructRequestMessagePayload getPayload() {
        return this.payload;
    }

    @JsonProperty("payload")
    public void setPayload(ConstructRequestMessage.ConstructRequestMessagePayload payload) {
       this.payload = payload;
    } 

    public class ConstructRequestMessagePayload extends BasicPayload {

        public ConstructRequestMessagePayload() {
        }
        
        @JsonProperty("graphManagerInput")
        private GraphManagerQueryInput graphManagerConstructInput;

        @JsonProperty("graphManagerInput")
        public GraphManagerQueryInput getGraphManagerInput() {
            return graphManagerConstructInput;
        }

        @JsonProperty("graphManagerInput")
        public void setGraphManagerInput(GraphManagerQueryInput graphManagerConstructInput) {
            this.graphManagerConstructInput = graphManagerConstructInput;
        }
    }
       
}
