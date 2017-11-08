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
public class IndexServiceResponseMessage extends AbstractMessage {
        @JsonProperty("payload")
    private IndexServiceResponsePayload payload;

    @JsonProperty("payload")
    public IndexServiceResponsePayload getPayload() {
        return this.payload;
    }

    @JsonProperty("payload")
    public void setPayload(IndexServiceResponsePayload payload) {
       this.payload = payload;
    } 
    
    public class IndexServiceResponsePayload {

        public IndexServiceResponsePayload() {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonPropertyOrder({
            "indexingServiceOutput"
        })

        @JsonProperty("indexingServiceInput")
        private String indexingServiceOutput;

        @JsonProperty("indexingServiceOutput")
        public String getIndexingServiceOutput() {
            return indexingServiceOutput;
        }

        @JsonProperty("indexingServiceInput")
        public void setIndexingServiceOutput(String indexingServiceOutput) {
            this.indexingServiceOutput = indexingServiceOutput;
        }
    }    
}
