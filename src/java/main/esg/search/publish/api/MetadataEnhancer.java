package esg.search.publish.api;

import esg.search.core.Record;


/**
 * Interface for augmenting a record metadata, on a field by field basis.
 * 
 * @author Luca Cinquini
 *
 */
public interface MetadataEnhancer {
    
    /**
     * Method to enhance a record metadata whenever a (name, value) pair is found.
     * @param name
     * @param values
     * @return
     */
    void enhance(String name, String value, Record record);

}
