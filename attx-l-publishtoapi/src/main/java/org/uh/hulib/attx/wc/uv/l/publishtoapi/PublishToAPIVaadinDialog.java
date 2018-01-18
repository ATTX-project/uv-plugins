package org.uh.hulib.attx.wc.uv.l.publishtoapi;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * Vaadin configuration dialog for DescribeDataSet.
 *
 * @author Unknown
 */
public class PublishToAPIVaadinDialog extends AbstractDialog<PublishToAPIConfig_V1> {

    private ObjectProperty<String> alias = new ObjectProperty<String>("");
    
    public PublishToAPIVaadinDialog() {
        super(PublishToAPI.class);
    }

    @Override
    public void setConfiguration(PublishToAPIConfig_V1 c) throws DPUConfigException {
        this.alias.setValue(c.getAlias());
    }

    @Override
    public PublishToAPIConfig_V1 getConfiguration() throws DPUConfigException {
        final PublishToAPIConfig_V1 c = new PublishToAPIConfig_V1();
        c.setAlias(this.alias.getValue());
        

        return c;
    }

    @Override
    public void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);

        //mainLayout.addComponent(new Label(ctx.tr("DescribeDataSet.dialog.label")));
        
        final TextField alias = new TextField("Alias", this.alias);
        mainLayout.addComponent(alias);        
        alias.setRequired(true);


        setCompositionRoot(mainLayout);
    }
}
