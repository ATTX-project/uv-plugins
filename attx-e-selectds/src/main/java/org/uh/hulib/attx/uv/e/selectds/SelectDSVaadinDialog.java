package org.uh.hulib.attx.uv.e.selectds;

import com.vaadin.data.Property;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Vaadin configuration dialog for SelectExistingDataSets.
 *
 * @author Unknown
 */
public class SelectDSVaadinDialog extends AbstractDialog<SelectDSConfig_V1> {

    public SelectDSVaadinDialog() {
        super(SelectDS.class);
    }
    
    Table graphsTable = null;
    ATTXClient attxClient = null;
    

    @Override
    public void setConfiguration(SelectDSConfig_V1 c) throws DPUConfigException {
        attxClient = new ATTXClient("graphmanager", "fuseki:3030/test");
        List<OptionValue> workingGraphs = attxClient.getWorkingGraphs();
        graphsTable.removeAllItems();
        for(OptionValue ov : workingGraphs) {
            CheckBox cb = new CheckBox();
            if(findOptionValue(c.getInputGraphs(), ov))
                cb.setValue(true);
            
            Object[] row = new Object[] {ov.getLabel(),cb};
            graphsTable.addItem(row, ov.getValue());
        }
        
    }

    @Override
    public SelectDSConfig_V1 getConfiguration() throws DPUConfigException {
        final SelectDSConfig_V1 c = new SelectDSConfig_V1();
        List<OptionValue> inputGraphs = new ArrayList<OptionValue>();
        for(Iterator i = graphsTable.getItemIds().iterator(); i.hasNext();) {
            String uri = (String)i.next();
            OptionValue ov = new OptionValue(uri, uri);
            ov.setLabel((String)graphsTable.getContainerProperty(uri,"label").getValue());
            Property prop = graphsTable.getContainerProperty(uri,"uri");
            CheckBox cb = (CheckBox)prop.getValue();
            System.out.println(prop.getType());
            System.out.println(cb.getValue());
            
            if(cb.getValue()) {
                inputGraphs.add(ov);
            }
        }
        c.setInputGraphs(inputGraphs);
        
        return c;
    }

    @Override
    public void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);

/*        
        final TextArea config = new TextArea("Dataset URIs");
        config.setRows(4);
        config.setWidth("100%");
        mainLayout.addComponent(config);
*/
        graphsTable = new Table("Input graphs");
        graphsTable.setPageLength(10);
        graphsTable.addContainerProperty("label", String.class,null);
        graphsTable.addContainerProperty("uri", CheckBox.class, null);
        graphsTable.setColumnHeaders("Label", "Check");
        graphsTable.setImmediate(true);
        //graphsTable.setEditable(true);
        
        mainLayout.addComponent(graphsTable);

        setCompositionRoot(mainLayout);
    }
    
    private boolean findOptionValue(List<OptionValue> haystack, OptionValue needle) {
        for(OptionValue ov : haystack) {
            if(ov.getValue().equals(needle.getValue()))
                return true;
        }
        return false;
    }    
}
