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
     * Method to report the most current data as a (string, integer) pairs.
     * @return
     * @throws Exception
     */
    public Map<String, Integer> report() throws Exception;

}
