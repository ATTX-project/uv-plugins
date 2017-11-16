package org.uh.hulib.attx.wc.uv.e.oaipmhharvester;

import java.util.Date;

/**
 * Configuration class for DescribeDataSet.
 *
 * @author Unknown
 */
public class OAIPMHHarvesterConfig_V1 {

    
    private String baseURL = "";
    private String metadataFormat = "";
    private String set = "";
    private boolean useDates = false;
    private Date fromDate = null;
    private Date untilDate = null;
    private boolean sinceLastSuccessful = false;

    public boolean isUseDates() {
        return useDates;
    }

    public void setUseDates(boolean useDates) {
        this.useDates = useDates;
    }

    
    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public String getMetadataFormat() {
        return metadataFormat;
    }

    public void setMetadataFormat(String metadataFormat) {
        this.metadataFormat = metadataFormat;
    }

    public String getSet() {
        return set;
    }

    public void setSet(String set) {
        this.set = set;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getUntilDate() {
        return untilDate;
    }

    public void setUntilDate(Date untilDate) {
        this.untilDate = untilDate;
    }

    public boolean isSinceLastSuccessful() {
        return sinceLastSuccessful;
    }

    public void setSinceLastSuccessful(boolean sinceLastSuccessful) {
        this.sinceLastSuccessful = sinceLastSuccessful;
    }


    public OAIPMHHarvesterConfig_V1() {

    }

}
