/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uh.hulib.attx.wc.uv.common.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jkesanie
 */
public class ProvenanceMessage extends AbstractMessage {
    @JsonProperty("payload")
    private Map<String, Object> payload = new HashMap<String, Object>();

    @JsonProperty("payload")
    public Map<String, Object> getPayload() {
        return this.payload;
    }

    @JsonProperty("payload")
    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

}
