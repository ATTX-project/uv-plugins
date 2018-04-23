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
public class OntologyServiceResponseMessage extends AbstractMessage {
    
    @JsonProperty("payload")
    private OntologyServiceResponseMessage.OntologyServiceResponseMessagePayload payload = new OntologyServiceResponseMessage.OntologyServiceResponseMessagePayload();

    @JsonProperty("payload")
    public OntologyServiceResponseMessage.OntologyServiceResponseMessagePayload getPayload() {
        return this.payload;
    }

    @JsonProperty("payload")
    public void setPayload(OntologyServiceResponseMessage.OntologyServiceResponseMessagePayload payload) {
       this.payload = payload;
    } 

    public class OntologyServiceResponseMessagePayload extends BasicPayload {

        public OntologyServiceResponseMessagePayload() {
        }
        
        @JsonProperty("ontologyServiceOutput")
        private OntologyServiceOutput ontologyServiceOutput;

        @JsonProperty("ontologyServiceOutput")
        public OntologyServiceOutput getOntologyServiceOutput() {
            return ontologyServiceOutput;
        }

        @JsonProperty("ontologyServiceOutput")
        public void setOntologyServiceOutput(OntologyServiceOutput ontologyServiceOutput) {
            this.ontologyServiceOutput = ontologyServiceOutput;
        }
    }
    
}
