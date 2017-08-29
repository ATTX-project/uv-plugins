/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uh.hulib.attx.wc.uv.t.rmlservice;

import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * Vaadin configuration dialog for RMLService.
 *
 * @author Unknown
 */
public class RMLServiceVaadinDialog extends AbstractDialog<RMLServiceConfig_V1> {

    public RMLServiceVaadinDialog() {
        super(RMLService.class);
    }

    @Override
    public void setConfiguration(RMLServiceConfig_V1 c) throws DPUConfigException {

    }

    @Override
    public RMLServiceConfig_V1 getConfiguration() throws DPUConfigException {
        final RMLServiceConfig_V1 c = new RMLServiceConfig_V1();

        return c;
    }

    @Override
    public void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);

        final TextArea config = new TextArea("RML configuration");
        config.setRows(40);
        config.setWidth("100%");
        mainLayout.addComponent(config);

        setCompositionRoot(mainLayout);
    }
}
