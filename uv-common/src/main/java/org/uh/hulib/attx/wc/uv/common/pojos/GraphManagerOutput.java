/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uh.hulib.attx.wc.uv.common.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author jkesanie
 */
    public class GraphManagerOutput {

        
        public GraphManagerOutput() {
        
        }
        
        @JsonProperty("count")
        private Integer count;
        @JsonProperty("quadCount")
        private Integer quadCount;
        @JsonProperty("tripleCount")
        private Integer tripleCount;

        @JsonProperty("count")
        public Integer getCount() {
            return count;
        }

        @JsonProperty("count")
        public void setCount(Integer count) {
            this.count = count;
        }

        @JsonProperty("quadCount")
        public Integer getQuadCount() {
            return quadCount;
        }

        @JsonProperty("quadCount")
        public void setQuadCount(Integer quadCount) {
            this.quadCount = quadCount;
        }

        @JsonProperty("tripleCount")
        public Integer getTripleCount() {
            return tripleCount;
        }

        @JsonProperty("tripleCount")
        public void setTripleCount(Integer tripleCount) {
            this.tripleCount = tripleCount;
        }

    }