package org.uh.hulib.attx.wc.uv.e.oaipmhharvester;

import ORG.oclc.oai.harvester2.verb.ListRecords;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.DataUnitUtils;
import eu.unifiedviews.helpers.dataunit.files.FilesHelper;
import eu.unifiedviews.helpers.dataunit.rdf.RdfDataUnitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.rdf.simple.WritableSimpleRdf;
import eu.unifiedviews.helpers.dpu.rdf.EntityBuilder;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.io.FileUtils;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.DC;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Main data processing unit class.
 *
 * @author Unknown
 */
@DPU.AsExtractor
public class OAIPMHHarvester extends AbstractDpu<OAIPMHHarvesterConfig_V1> {

    private static final Logger log = LoggerFactory.getLogger(OAIPMHHarvester.class);

    @DataUnit.AsOutput(name = "harvestedFiles")
    public WritableFilesDataUnit harvesterFiles;

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    
	public OAIPMHHarvester() {
		super(OAIPMHHarvesterVaadinDialog.class, ConfigHistory.noHistory(OAIPMHHarvesterConfig_V1.class));
	}
		
    @Override
    protected void innerExecute() throws DPUException {

        ContextUtils.sendShortInfo(ctx, "Start harvesting");
        try {
            
            String baseURL = config.getBaseURL();
            String metadataFormat = config.getMetadataFormat();
            String set = config.getSet();
            
            boolean useDates = config.isUseDates();
            Date fromDate = config.getFromDate();
            Date untilDate = config.getUntilDate();
            ListRecords listRecords = null;
            if(useDates) {
                listRecords = new ListRecords(baseURL, 
                                            format.format(fromDate), 
                                            format.format(untilDate),
                                            set,
                                            metadataFormat);
            }
            else {
                listRecords = new ListRecords(baseURL, 
                                            null,
                                            null,
                                            set,
                                            metadataFormat);
                
            }
            int page = 0;
            while (listRecords != null) {
                NodeList errors = listRecords.getErrors();
                if (errors != null && errors.getLength() > 0) {
                    System.out.println("Found errors");
                    int length = errors.getLength();
                    for (int i=0; i<length; ++i) {
                        Node item = errors.item(i);
                        System.out.println(item);
                    }
                    System.out.println("Error record: " + listRecords.toString());
                    break;
                }
                System.out.println(listRecords);
                FilesDataUnit.Entry newFileEntry = FilesHelper.createFile(harvesterFiles, "harvestedFile_" + page);
                File outputFile = new File(URI.create(newFileEntry.getFileURIString()));
                FileUtils.write(outputFile, "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
                FileUtils.write(outputFile,listRecords.toString() , true);
                
                String resumptionToken = listRecords.getResumptionToken();
                System.out.println("resumptionToken: " + resumptionToken);
                if (resumptionToken == null || resumptionToken.length() == 0) {
                    listRecords = null;
                } else {
                    listRecords = new ListRecords(baseURL, resumptionToken);
                }
                page++;
                if(page > 2)
                    break;
            }
            
            ContextUtils.sendShortInfo(ctx, "Files harvested");
            
        }catch(Exception ex) {
            ex.printStackTrace();
            ContextUtils.sendError(ctx, "Harvesting failed", ex, ex.getMessage());
        }
        
        
    }
	
       
}
