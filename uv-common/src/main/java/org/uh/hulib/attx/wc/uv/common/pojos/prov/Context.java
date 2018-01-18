
package org.uh.hulib.attx.wc.uv.common.pojos.prov;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "workflowID",
    "activityID",
    "stepID"
})
public class Context {

    @JsonProperty("workflowID")
    private String workflowID;
    @JsonProperty("activityID")
    private String activityID;
    @JsonProperty("stepID")
    private String stepID;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("workflowID")
    public String getWorkflowID() {
        return workflowID;
    }

    @JsonProperty("workflowID")
    public void setWorkflowID(String workflowID) {
        this.workflowID = workflowID;
    }

    @JsonProperty("activityID")
    public String getActivityID() {
        return activityID;
    }

    @JsonProperty("activityID")
    public void setActivityID(String activityID) {
        this.activityID = activityID;
    }

    @JsonProperty("stepID")
    public String getStepID() {
        return stepID;
    }

    @JsonProperty("stepID")
    public void setStepID(String stepID) {
        this.stepID = stepID;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
