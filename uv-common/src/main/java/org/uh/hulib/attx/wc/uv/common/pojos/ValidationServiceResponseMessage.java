package org.uh.hulib.attx.wc.uv.common.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ValidationServiceResponseMessage extends AbstractMessage {
    @JsonProperty("payload")
    private ValidationServiceResponseMessage.ValidationServiceResponseMessagePayload payload = new ValidationServiceResponseMessage.ValidationServiceResponseMessagePayload();

    @JsonProperty("payload")
    public ValidationServiceResponseMessage.ValidationServiceResponseMessagePayload getPayload() {
        return this.payload;
    }

    @JsonProperty("payload")
    public void setPayload(ValidationServiceResponseMessage.ValidationServiceResponseMessagePayload payload) {
        this.payload = payload;
    }

    public class ValidationServiceResponseMessagePayload extends BasicPayload {

        public ValidationServiceResponseMessagePayload() {
        }

        @JsonProperty("validationServiceOutput")
        private ValidationServiceOutput validationServiceOutput;

        @JsonProperty("validationServiceOutput")
        public ValidationServiceOutput getValidationServiceOutput() {
            return validationServiceOutput;
        }

        @JsonProperty("validationServiceOutput")
        public void setValidationServiceOutput(ValidationServiceOutput validationServiceOutput) {
            this.validationServiceOutput = validationServiceOutput;
        }
    }
}
