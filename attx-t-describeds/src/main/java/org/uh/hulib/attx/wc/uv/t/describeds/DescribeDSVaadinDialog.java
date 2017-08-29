package org.uh.hulib.attx.wc.uv.t.describeds;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * Vaadin configuration dialog for DescribeDataSet.
 *
 * @author Unknown
 */
public class DescribeDSVaadinDialog extends AbstractDialog<DescribeDSConfig_V1> {

    private ObjectProperty<String> title = new ObjectProperty<String>("");
    private ObjectProperty<String> description = new ObjectProperty<String>("");
    
    public DescribeDSVaadinDialog() {
        super(DescribeDS.class);
    }

    @Override
    public void setConfiguration(DescribeDSConfig_V1 c) throws DPUConfigException {
        this.title.setValue(c.getTitle());
        this.description.setValue(c.getDescription());
    }

    @Override
    public DescribeDSConfig_V1 getConfiguration() throws DPUConfigException {
        final DescribeDSConfig_V1 c = new DescribeDSConfig_V1();
        c.setTitle(this.title.getValue());
        c.setDescription(this.description.getValue());

        return c;
    }

    @Override
    public void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);

        //mainLayout.addComponent(new Label(ctx.tr("DescribeDataSet.dialog.label")));
        
        final TextField title = new TextField("Title", this.title);
        mainLayout.addComponent(title);        
        title.setRequired(true);

        final TextArea desc = new TextArea("Description", this.description);
        desc.setRows(4);
        desc.setWidth("100%");
        mainLayout.addComponent(desc);

        setCompositionRoot(mainLayout);
    }
}
