package org.uh.hulib.attx.wc.uv.e.oaipmhharvester;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;
import java.util.Date;

/**
 * Vaadin configuration dialog for DescribeDataSet.
 *
 * @author Unknown
 */
public class OAIPMHHarvesterVaadinDialog extends AbstractDialog<OAIPMHHarvesterConfig_V1> {

    private ObjectProperty<String> baseURL = new ObjectProperty<String>("");
    
    private ObjectProperty<String> metadataFormat = new ObjectProperty<String>("");
    private ObjectProperty<String> set = new ObjectProperty<String>("");
    private ObjectProperty<Date> fromDate = new ObjectProperty<Date>(new Date());    
    private ObjectProperty<Date> untilDate = new ObjectProperty<Date>(new Date());
    private ObjectProperty<Boolean> sinceLastSuccessful = new ObjectProperty<Boolean>(false);
    private ObjectProperty<Boolean> useDates = new ObjectProperty<Boolean>(false);
    
    public OAIPMHHarvesterVaadinDialog() {
        super(OAIPMHHarvester.class);
    }

    @Override
    public void setConfiguration(OAIPMHHarvesterConfig_V1 c) throws DPUConfigException {
        this.baseURL.setValue(c.getBaseURL());
        
        this.metadataFormat.setValue(c.getMetadataFormat());
        this.set.setValue(c.getSet());
        this.fromDate.setValue(c.getFromDate());        
        this.untilDate.setValue(c.getUntilDate());
        this.sinceLastSuccessful.setValue(c.isSinceLastSuccessful());
        this.useDates.setValue(c.isUseDates());
    }

    @Override
    public OAIPMHHarvesterConfig_V1 getConfiguration() throws DPUConfigException {
        final OAIPMHHarvesterConfig_V1 c = new OAIPMHHarvesterConfig_V1();
        c.setBaseURL(this.baseURL.getValue());
        
        c.setMetadataFormat(this.metadataFormat.getValue());
        c.setSet(this.set.getValue());
        c.setFromDate(this.fromDate.getValue());        
        c.setUntilDate(this.untilDate.getValue());
        c.setSinceLastSuccessful(this.sinceLastSuccessful.getValue());
        c.setUseDates(this.useDates.getValue());
        return c;
    }

    @Override
    public void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);

        //mainLayout.addComponent(new Label(ctx.tr("DescribeDataSet.dialog.label")));
        
        final TextField baseURLField = new TextField("baseURL", this.baseURL);
        baseURLField.setRequired(true);
        mainLayout.addComponent(baseURLField);        

        final TextField metadataFormat = new TextField("metadataFormat", this.metadataFormat);
        metadataFormat.setRequired(true);
        mainLayout.addComponent(metadataFormat);        

        final TextField set = new TextField("set", this.set);
        set.setRequired(false);
        mainLayout.addComponent(set);

        final CheckBox useDates = new CheckBox("Use dates", this.useDates);
        mainLayout.addComponent(useDates);

        
        final DateField fromDate = new DateField("from", this.fromDate);
        fromDate.setDateFormat("yyyy-MM-dd");
        fromDate.setRequired(false);        
        mainLayout.addComponent(fromDate);
        
        final DateField untilDate = new DateField("until", this.untilDate);
        untilDate.setDateFormat("yyyy-MM-dd");
        untilDate.setRequired(false);        
        mainLayout.addComponent(untilDate);
        
        final CheckBox sinceLast = new CheckBox("since last successful", this.sinceLastSuccessful);
        mainLayout.addComponent(sinceLast);
    

        setCompositionRoot(mainLayout);
    }
}
