package esg.search.publish.thredds.parsers;

import thredds.catalog.InvDataset;
import esg.search.core.Record;

/**
 * Generic interface for parsing a THREDDS XML element into Record metadata.
 * 
 * @author Luca Cinquini
 *
 */
public interface ThreddsElementParser {
    
    /**
     * 
     * @param dataset input THREDDS dataset
     * @param record output metadata record
     * @param ds container for high-level dataset metadata
     */
    void parse(InvDataset dataset, Record record, DatasetSummary ds);

}
