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
public class RMLServiceRequestMessage extends AbstractMessage {

    @JsonProperty("payload")
    private RMLServiceInput payload;

    @JsonProperty("payload")
    public RMLServiceInput getPayload() {
        return this.payload;
    }

    @JsonProperty("payload")
    public void setPayload(RMLServiceInput payload) {
        this.payload = payload;
    }
    
    public class RMLServiceRequestPayload {

        public RMLServiceRequestPayload() {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonPropertyOrder({
            "rmlserviceInput"
        })

        @JsonProperty("rmlserviceInput")
        private RMLServiceInput rmlserviceInput;

        @JsonProperty("rmlserviceInput")
        public RMLServiceInput getRMLServiceInput() {
            return rmlserviceInput;
        }

        @JsonProperty("graphManagerInput")
        public void setRMLServiceInput(RMLServiceInput rmlserviceInput) {
            this.rmlserviceInput = rmlserviceInput;
        }
    }

}
