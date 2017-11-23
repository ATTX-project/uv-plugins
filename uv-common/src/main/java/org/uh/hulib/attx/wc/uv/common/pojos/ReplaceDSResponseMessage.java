/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uh.hulib.attx.wc.uv.common.pojos;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.HashMap;
import java.util.Map;
import org.uh.hulib.attx.wc.uv.common.pojos.prov.Provenance;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReplaceDSResponseMessage extends AbstractMessage {

    @JsonProperty("payload")
    private ReplaceDSResponsePayload payload;

    @JsonProperty("payload")
    public ReplaceDSResponsePayload getPayload() {
        return payload;
    }

    @JsonProperty("payload")
    public void setPayload(ReplaceDSResponsePayload payload) {
        this.payload = payload;
    }


    public class ReplaceDSResponsePayload extends BasicPayload {

        public ReplaceDSResponsePayload() {

        }


    }


}
