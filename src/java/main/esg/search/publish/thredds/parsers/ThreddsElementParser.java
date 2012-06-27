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
    
    void parse(InvDataset dataset, Record record);

}
