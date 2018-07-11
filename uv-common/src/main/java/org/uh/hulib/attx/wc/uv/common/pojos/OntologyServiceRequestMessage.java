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
public class OntologyServiceRequestMessage extends AbstractMessage {
    
    @JsonProperty("payload")
    private OntologyServiceRequestMessage.OntologyServiceRequestMessagePayload payload = new OntologyServiceRequestMessage.OntologyServiceRequestMessagePayload();

    @JsonProperty("payload")
    public OntologyServiceRequestMessage.OntologyServiceRequestMessagePayload getPayload() {
        return this.payload;
    }

    @JsonProperty("payload")
    public void setPayload(OntologyServiceRequestMessage.OntologyServiceRequestMessagePayload payload) {
       this.payload = payload;
    } 

    public class OntologyServiceRequestMessagePayload {

        public OntologyServiceRequestMessagePayload() {
        }
        
        @JsonProperty("ontologyServiceInput")
        private OntologyServiceInput ontologyServiceInput;

        @JsonProperty("ontologyServiceInput")
        public OntologyServiceInput getOntologyServiceInput() {
            return ontologyServiceInput;
        }

        @JsonProperty("ontologyServiceInput")
        public void setOntologyServiceInput(OntologyServiceInput ontologyServiceInput) {
            this.ontologyServiceInput = ontologyServiceInput;
        }
    }
    
}
