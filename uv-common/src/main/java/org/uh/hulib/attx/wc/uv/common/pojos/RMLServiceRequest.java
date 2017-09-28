/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uh.hulib.attx.wc.uv.common.pojos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RMLServiceRequest extends AbstractMessage {

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

}
