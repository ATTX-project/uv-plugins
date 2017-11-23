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
public class FramingRequestMessage extends AbstractMessage {
    
    @JsonProperty("payload")
    private FramingRequestMessage.FramingRequestMessagePayload payload = new FramingRequestMessage.FramingRequestMessagePayload();

    @JsonProperty("payload")
    public FramingRequestMessage.FramingRequestMessagePayload getPayload() {
        return this.payload;
    }

    @JsonProperty("payload")
    public void setPayload(FramingRequestMessage.FramingRequestMessagePayload payload) {
       this.payload = payload;
    } 

    public class FramingRequestMessagePayload extends BasicPayload {

        public FramingRequestMessagePayload() {
        }
        
        @JsonProperty("framingServiceInput")
        private FramingServiceInput framingServiceInput;

        @JsonProperty("framingServiceInput")
        public FramingServiceInput getGraphManagerInput() {
            return framingServiceInput;
        }

        @JsonProperty("framingServiceInput")
        public void setGraphManagerInput(FramingServiceInput framingServiceInput) {
            this.framingServiceInput = framingServiceInput;
        }
    }
    
}
