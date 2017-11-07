/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uh.hulib.attx.wc.uv.l.replaceds;

import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * Vaadin configuration dialog for RMLService.
 *
 * @author Unknown
 */
public class ReplaceDSVaadinDialog extends AbstractDialog<ReplaceDSConfig_V1> {

    private NativeSelect graphActivity = new NativeSelect();

    
    public ReplaceDSVaadinDialog() {
        super(ReplaceDS.class);
    }

    @Override
    public void setConfiguration(ReplaceDSConfig_V1 c) throws DPUConfigException {
        this.graphActivity.setValue(c.getGraphActivity());
    }

    @Override
    public ReplaceDSConfig_V1 getConfiguration() throws DPUConfigException {
        final ReplaceDSConfig_V1 c = new ReplaceDSConfig_V1();

        c.setGraphActivity(this.graphActivity.getValue().toString());
        return c;
    }

    @Override
    public void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);
        
        graphActivity.setCaption("Action");        
        graphActivity.addItem("replace");
        graphActivity.addItem("add");
        graphActivity.setValue("replace");        
        graphActivity.setRequired(true);        
        mainLayout.addComponent(graphActivity);
        
        setCompositionRoot(mainLayout);
    }
}
