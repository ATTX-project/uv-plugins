package org.uh.hulib.attx.wc.uv.t.describeexternalds;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * Vaadin configuration dialog for DescribeExternalDataSource.
 *
 * @author Unknown
 */
public class DescribeExternalDSVaadinDialog extends AbstractDialog<DescribeExternalDSConfig_V1> {

    private ObjectProperty<String> title = new ObjectProperty<String>("");
    private ObjectProperty<String> description = new ObjectProperty<String>("");
    private NativeSelect inputGraphLicence = new NativeSelect();

    public DescribeExternalDSVaadinDialog() {
        super(DescribeExternalDS.class);
    }

    @Override
    public void setConfiguration(DescribeExternalDSConfig_V1 c) throws DPUConfigException {
        this.title.setValue(c.getTitle());
        this.description.setValue(c.getDescription());
        this.inputGraphLicence.setValue(c.getLicence());
    }

    @Override
    public DescribeExternalDSConfig_V1 getConfiguration() throws DPUConfigException {
        final DescribeExternalDSConfig_V1 c = new DescribeExternalDSConfig_V1();
        c.setTitle(this.title.getValue());
        c.setDescription(this.description.getValue());
        c.setLicence(this.inputGraphLicence.getValue().toString());
        return c;
    }

    @Override
    public void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);

        //mainLayout.addComponent(new Label(ctx.tr("DescribeExternalDataSource.dialog.label")));
        
        final TextField title = new TextField("Title", this.title);
        title.setRequired(true);
        mainLayout.addComponent(title);    
        

        final TextArea desc = new TextArea("Description", this.description);
        desc.setRows(4);
        desc.setWidth("100%");
        mainLayout.addComponent(desc);

        
        inputGraphLicence.setCaption("Input graph license:");        
        inputGraphLicence.addItem("http://data.hulib.helsinki.fi/attx/onto#Unknown");
        inputGraphLicence.addItem("http://data.hulib.helsinki.fi/attx/onto#CC0");
        inputGraphLicence.setValue("http://data.hulib.helsinki.fi/attx/onto#Unknown");
        inputGraphLicence.setRequired(true);
        mainLayout.addComponent(inputGraphLicence);
        

        setCompositionRoot(mainLayout);
    }
}
