package org.uh.hulib.attx.wc.uv.common.pojos.prov;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "context",
    "agent",
    "activity",
    "input",
    "output"
})
public class Provenance {

    @JsonProperty("context")
    private Context context;
    @JsonProperty("agent")
    private Agent agent;
    @JsonProperty("activity")
    private Activity activity;
    @JsonProperty("input")
    private List<DataProperty> input = null;
    @JsonProperty("output")
    private List<DataProperty> output = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("context")
    public Context getContext() {
        return context;
    }

    @JsonProperty("context")
    public void setContext(Context context) {
        this.context = context;
    }

    @JsonProperty("agent")
    public Agent getAgent() {
        return agent;
    }

    @JsonProperty("agent")
    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    @JsonProperty("activity")
    public Activity getActivity() {
        return activity;
    }

    @JsonProperty("activity")
    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    @JsonProperty("input")
    public List<DataProperty> getInput() {
        return input;
    }

    @JsonProperty("input")
    public void setInput(List<DataProperty> input) {
        this.input = input;
    }

    @JsonProperty("output")
    public List<DataProperty> getOutput() {
        return output;
    }

    @JsonProperty("output")
    public void setOutput(List<DataProperty> output) {
        this.output = output;
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
