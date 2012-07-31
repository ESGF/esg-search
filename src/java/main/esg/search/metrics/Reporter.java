package esg.search.metrics;

import java.util.Map;

/**
 * Generic interface for collecting node metrics.
 * 
 * @author Luca Cinquini
 *
 */
public interface Reporter {
    
    /**
     * Method to report the most current data as a map of (string, integer) pairs.
     * 
     * @return
     * @throws Exception
     */
    public Map<String, Integer> report() throws Exception;
    
    /**
     * Returns the path to the CSV file storing all metrics.
     * @return
     */
    public String getCsvFilePath();
    
    /**
     * Returns the path to the XML file storing the latest metrics.
     * @return
     */
    public String getXmlFilePath();
    
    /**
     * Returns a unique name for the type of metrics being collected.
     * @return
     */
    public String getReportType();

}
