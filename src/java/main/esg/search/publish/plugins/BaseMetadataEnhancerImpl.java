package esg.search.publish.plugins;

import java.util.HashSet;
import java.util.Set;


/**
 * Useful superclass for {@link MetadataEnhancer} implementations.
 * 
 * @author Luca Cinquini
 *
 */
public abstract class BaseMetadataEnhancerImpl implements MetadataEnhancer {
    
    private Set<String> recordTypes = new HashSet<String>();
    
    public BaseMetadataEnhancerImpl() {}

    @Override
    public boolean forType(final String recordType) {
        return recordTypes.contains(recordType);
    }
    
    /**
     * Method to set multiple record types.
     * @param recordTypes
     */
    public void setTypes(final Set<String> recordTypes) {
        this.recordTypes = recordTypes;
    }
    
    /**
     * Method to set a single record type.
     * @param recordType
     */
    public void setType(final String recordType) {
        this.recordTypes.add(recordType);
    }
    

}
