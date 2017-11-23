/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uh.hulib.attx.wc.uv.common.pojos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RMLServiceResponseMessage extends AbstractMessage {

     @JsonProperty("payload")
    private RMLServiceResponsePayload payload;

    @JsonProperty("payload")
    public RMLServiceResponsePayload getPayload() {
        return this.payload;
    }

    @JsonProperty("payload")
    public void setPayload(RMLServiceResponsePayload payload) {
        this.payload = payload;
    }
    
    public class RMLServiceResponsePayload extends BasicPayload {

        public RMLServiceResponsePayload() {
        }

        @JsonProperty("rmlserviceOutput")
        private RMLServiceOutput rmlserviceOutput;

        @JsonProperty("rmlserviceOutput")
        public RMLServiceOutput getRMLServiceOutput() {
            return rmlserviceOutput;
        }

        @JsonProperty("graphManagerOutput")
        public void setRMLServiceOutput(RMLServiceOutput rmlserviceOutput) {
            this.rmlserviceOutput = rmlserviceOutput;
        }
    }    

}