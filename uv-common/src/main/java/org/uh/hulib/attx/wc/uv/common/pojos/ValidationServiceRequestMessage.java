package org.uh.hulib.attx.wc.uv.common.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ValidationServiceRequestMessage  extends AbstractMessage {
    @JsonProperty("payload")
    private ValidationServiceRequestMessage.ValidationServiceRequestMessagePayload payload = new ValidationServiceRequestMessage.ValidationServiceRequestMessagePayload();

    @JsonProperty("payload")
    public ValidationServiceRequestMessage.ValidationServiceRequestMessagePayload getPayload() {
        return this.payload;
    }

    @JsonProperty("payload")
    public void setPayload(ValidationServiceRequestMessage.ValidationServiceRequestMessagePayload payload) {
        this.payload = payload;
    }

    public class ValidationServiceRequestMessagePayload {

        public ValidationServiceRequestMessagePayload() {
        }

        @JsonProperty("validationServiceInput")
        private ValidationServiceInput validationServiceInput;

        @JsonProperty("validationServiceInput")
        public ValidationServiceInput getValidationServiceInput() {
            return validationServiceInput;
        }

        @JsonProperty("validationServiceInput")
        public void setValidationServiceInput(ValidationServiceInput validationServiceInput) {
            this.validationServiceInput = validationServiceInput;
        }
    }
}
