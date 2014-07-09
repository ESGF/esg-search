package esg.search.publish.plugins;

import java.util.HashSet;
import java.util.Set;

import esg.search.query.api.QueryParameters;


/**
 * Useful superclass for {@link MetadataEnhancer} implementations.
 * 
 * @author Luca Cinquini
 *
 */
public abstract class BaseMetadataEnhancerImpl implements MetadataEnhancer {
    
    private Set<String> recordTypes = new HashSet<String>();
    
    // by default, each MetadataEnhancer applies to the "type" field (which all records have)
    private String fieldName = QueryParameters.FIELD_TYPE;
    
    public BaseMetadataEnhancerImpl() {}

    @Override
    public boolean forType(final String recordType) {
        return recordTypes.contains(recordType);
    }
    
    @Override
    public String forField() {
    	return this.fieldName;
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
    
    /**
     * Method to configure this metadata enhancer to apply to a specifc field
     * @param fieldName
     */
    public void setField(final String fieldName) {
    	this.fieldName = fieldName;
    }
    

}
