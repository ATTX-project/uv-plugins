/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uh.hulib.attx.wc.uv.t.framingservice;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * Vaadin configuration dialog for FramingService.
 *
 * @author Unknown
 */
public class FramingServiceVaadinDialog extends AbstractDialog<FramingServiceConfig_V1> {

    private ObjectProperty<String> configuration = new ObjectProperty<String>("");
    private ObjectProperty<String> docType = new ObjectProperty<String>("");

    public FramingServiceVaadinDialog() {
        super(FramingService.class);
    }

    @Override
    public void setConfiguration(FramingServiceConfig_V1 c) throws DPUConfigException {
        this.configuration.setValue(c.getConfiguration());
        this.docType.setValue(c.getDocType());
    }

    @Override
    public FramingServiceConfig_V1 getConfiguration() throws DPUConfigException {
        final FramingServiceConfig_V1 c = new FramingServiceConfig_V1();
        c.setConfiguration(this.configuration.getValue());
        c.setDocType(this.docType.getValue());
        return c;
    }

    @Override
    public void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);

        final TextField docType = new TextField("Document type", this.docType);
        mainLayout.addComponent(docType);        
        docType.setRequired(false);

        
        final TextArea config = new TextArea(this.configuration);
        config.setCaption("Frame configuration");
        config.setRows(40);
        config.setWidth("100%");
        mainLayout.addComponent(config);

        setCompositionRoot(mainLayout);
    }
}
