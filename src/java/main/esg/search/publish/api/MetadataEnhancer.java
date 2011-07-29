package esg.search.publish.api;

import java.util.List;
import java.util.Map;

/**
 * Interface for augmenting the metadata parsed from a repository,
 * on a field by field basis
 * 
 * @author Luca Cinquini
 *
 */
public interface MetadataEnhancer {
    
    /**
     * Method to add a map of (name, values) metadata pairs whenever a (name, value) metadata pair is found.
     * @param name
     * @param values
     * @return
     */
    Map<String, List<String>> enhance(String name, String value);

}
