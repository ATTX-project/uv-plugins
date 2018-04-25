/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uh.hulib.attx.wc.uv.common.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jkesanie
 */
public class OntologyServiceInput {
    @JsonProperty("activity")
    private String activity;
    @JsonProperty("sourceData")
    private List<OntologyServiceSource> sourceData = new ArrayList<OntologyServiceSource>();

    @JsonProperty("activity")
    public String getActivity() {
        return activity;
    }

    @JsonProperty("activity")
    public void setActivity(String activity) {
        this.activity = activity;
    }

    @JsonProperty("sourceData")
    public List<OntologyServiceSource> getSourceData() {
        return sourceData;
    }

    @JsonProperty("sourceData")
    public void setSourceData(List<OntologyServiceSource> sourceData) {
        this.sourceData = sourceData;
    }
    
    public class OntologyServiceSource {
        @JsonProperty("schemaGraph")
        private String schemaGraph;
        @JsonProperty("dataGraph")
        private String dataGraph;

        @JsonProperty("schemaGraph")
        public String getSchemaGraph() {
            return schemaGraph;
        }

        @JsonProperty("schemaGraph")
        public void setSchemaGraph(String schemaGraph) {
            this.schemaGraph = schemaGraph;
        }

        @JsonProperty("dataGraph")
        public String getDataGraph() {
            return dataGraph;
        }

        @JsonProperty("dataGraph")
        public void setDataGraph(String dataGraph) {
            this.dataGraph = dataGraph;
        }

        
    }
    
}
