/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uh.hulib.attx.wc.uv.common.pojos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 *
 * @author jkesanie
 */
public class IndexServiceRequestMessage extends AbstractMessage {
        @JsonProperty("payload")
    private IndexServiceRequestPayload payload;

    @JsonProperty("payload")
    public IndexServiceRequestPayload getPayload() {
        return this.payload;
    }

    @JsonProperty("payload")
    public void setPayload(IndexServiceRequestPayload payload) {
       this.payload = payload;
    } 
    
    public class IndexServiceRequestPayload extends BasicPayload {

        public IndexServiceRequestPayload() {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonPropertyOrder({
            "indexingServiceInput"
        })

        @JsonProperty("indexingServiceInput")
        private IndexServiceInput indexingServiceInput;

        @JsonProperty("indexingServiceInput")
        public IndexServiceInput getGraphManagerInput() {
            return indexingServiceInput;
        }

        @JsonProperty("indexingServiceInput")
        public void setGraphManagerInput(IndexServiceInput indexingServiceInput) {
            this.indexingServiceInput = indexingServiceInput;
        }
    }    
}
