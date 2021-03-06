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
public class FramingResponseMessage extends AbstractMessage {
    
    @JsonProperty("payload")
    private FramingResponseMessage.FramingResponseMessagePayload payload = new FramingResponseMessage.FramingResponseMessagePayload();

    @JsonProperty("payload")
    public FramingResponseMessage.FramingResponseMessagePayload getPayload() {
        return this.payload;
    }

    @JsonProperty("payload")
    public void setPayload(FramingResponseMessage.FramingResponseMessagePayload payload) {
       this.payload = payload;
    } 

    public class FramingResponseMessagePayload extends BasicPayload {

        public FramingResponseMessagePayload() {
        }
        
        @JsonProperty("framingServiceOutput")
        private FramingServiceOutput framingServiceOutput;

        @JsonProperty("framingServiceOutput")
        public FramingServiceOutput getFramingServiceOutput() {
            return framingServiceOutput;
        }

        @JsonProperty("framingServiceOutput")
        public void setFramingServiceOutput(FramingServiceOutput framingServiceOutput) {
            this.framingServiceOutput = framingServiceOutput;
        }
    }
    
}
