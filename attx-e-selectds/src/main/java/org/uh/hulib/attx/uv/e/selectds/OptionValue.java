/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uh.hulib.attx.uv.e.selectds;

import java.io.Serializable;

/**
 *
 * @author jkesanie
 */
public class OptionValue implements Serializable {
    private String value;
    private String label;
    
    public OptionValue(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    
    public String getValue() {
        return this.value;
    }
    
    public String getLabel() {
        return this.label;
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }
    
    
}
