package esg.search.publish.plugins;

import java.util.List;
import java.util.Properties;

import org.springframework.util.StringUtils;

import esg.search.core.Record;

/**
 * Class that enhances the published metadata from a static properties file.
 * 
 * Specifically, the configured metadata key is looked up in the properties file
 * and added to the field records.
 * 
 * @author Luca Cinquini
 *
 */
public class StaticPropertiesMetadataEnhancer extends BaseMetadataEnhancerImpl {
    
    private final Properties properties;
    
    /*
     * The property lookup key.
     */
    private final String key;
    
    /**
     * The name of the metadata field to assign the property value to.
     */
    private final String name;
        
    /**
     * Constructor
     * @param properties : the static properties file that contains additional metadata to be added to the records.
     */
    public StaticPropertiesMetadataEnhancer(final String key, final String name, final Properties properties) {
        this.key = key;
        this.name = name.toLowerCase();
        this.properties = properties;
    }

    /**
     * This method implementation completely disregards the supplied (name, values) 
     * and uses the instance key as lookup into the properties file.
     */
    @Override
    public void enhance(final String name, final List<String> values, final Record record) {
                
        if (StringUtils.hasText(properties.getProperty(this.key))) {
            record.addField(this.name, properties.getProperty(this.key));
        }
                
    }

}
