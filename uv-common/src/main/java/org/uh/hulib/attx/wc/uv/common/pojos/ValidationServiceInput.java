package org.uh.hulib.attx.wc.uv.common.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ValidationServiceInput {

    @JsonProperty("sourceData")
    private List<Source> sourceData = null;

    @JsonProperty("sourceData")
    public List<Source> getSourceData() {
        return sourceData;
    }

    @JsonProperty("sourceData")
    public void setSourceData(List<Source> sourceData) {
        this.sourceData = sourceData;
    }

}
