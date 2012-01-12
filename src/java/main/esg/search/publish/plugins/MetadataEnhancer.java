package esg.search.publish.plugins;

import java.util.List;
import java.util.Set;

import esg.search.core.Record;


/**
 * Interface for augmenting a record metadata, on a field by field basis.
 * 
 * @author Luca Cinquini
 *
 */
public interface MetadataEnhancer {
    
    /**
     * Method to enhance a record metadata whenever a (name, value[]) pair is found.
     * @param name
     * @param values
     * @return
     */
    void enhance(String name, List<String> values, Record record);
    
    /**
     * Method to determine if these metadata enhancements should be applied to the given record type.
     * @return
     */
    boolean forType(String recordType);

}
