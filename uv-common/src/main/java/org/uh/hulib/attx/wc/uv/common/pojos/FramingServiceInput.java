/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uh.hulib.attx.wc.uv.common.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 *
 * @author jkesanie
 */
public class FramingServiceInput {
    @JsonProperty("docType")
    private String docType;
    @JsonProperty("ldFrame")
    private String ldFrame;
    @JsonProperty("sourceData")
    private List<Source> sourceData = null;

    @JsonProperty("docType")
    public String getDocType() {
        return docType;
    }

    @JsonProperty("docType")
    public void setDocType(String docType) {
        this.docType = docType;
    }

    @JsonProperty("ldFrame")
    public String getLdFrame() {
        return ldFrame;
    }

    @JsonProperty("ldFrame")
    public void setLdFrame(String ldFrame) {
        this.ldFrame = ldFrame;
    }

    @JsonProperty("sourceData")
    public List<Source> getSourceData() {
        return sourceData;
    }

    @JsonProperty("sourceData")
    public void setSourceData(List<Source> sourceData) {
        this.sourceData = sourceData;
    }

    
    
    
}
