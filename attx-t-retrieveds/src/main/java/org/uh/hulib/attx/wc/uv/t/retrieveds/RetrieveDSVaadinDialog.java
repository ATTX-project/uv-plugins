/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uh.hulib.attx.wc.uv.t.retrieveds;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * Vaadin configuration dialog for RetrieveDS.
 *
 * @author Unknown
 */
public class RetrieveDSVaadinDialog extends AbstractDialog<RetrieveDSConfig_V1> {

     private NativeSelect select = new NativeSelect();

    public RetrieveDSVaadinDialog() {
        super(RetrieveDS.class);
    }

    @Override
    public void setConfiguration(RetrieveDSConfig_V1 c) throws DPUConfigException {
        this.select.setValue(c.getOutputType());
    }

    @Override
    public RetrieveDSConfig_V1 getConfiguration() throws DPUConfigException {
        final RetrieveDSConfig_V1 c = new RetrieveDSConfig_V1();
        c.setOutputType(this.select.getValue().toString());
        return c;
    }

    @Override
    public void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);

        select.setCaption("Output type");        
        select.addItem("URI");
        select.addItem("Data");
        select.setValue("URI");        
        select.setRequired(true);        
        mainLayout.addComponent(select);

        setCompositionRoot(mainLayout);
    }
}
