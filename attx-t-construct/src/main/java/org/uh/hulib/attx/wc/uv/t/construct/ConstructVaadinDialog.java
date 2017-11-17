/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uh.hulib.attx.wc.uv.t.construct;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * Vaadin configuration dialog for Construct.
 *
 * @author Unknown
 */
public class ConstructVaadinDialog extends AbstractDialog<ConstructConfig_V1> {

    private ObjectProperty<String> configuration = new ObjectProperty<String>("");

    public ConstructVaadinDialog() {
        super(Construct.class);
    }

    @Override
    public void setConfiguration(ConstructConfig_V1 c) throws DPUConfigException {
        this.configuration.setValue(c.getConfiguration());
    }

    @Override
    public ConstructConfig_V1 getConfiguration() throws DPUConfigException {
        final ConstructConfig_V1 c = new ConstructConfig_V1();
        c.setConfiguration(this.configuration.getValue());
        return c;
    }

    @Override
    public void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);

        final TextArea config = new TextArea(this.configuration);
        config.setCaption("RML configuration");
        config.setRows(40);
        config.setWidth("100%");
        mainLayout.addComponent(config);

        setCompositionRoot(mainLayout);
    }
}
