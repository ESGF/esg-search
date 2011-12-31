package esg.search.publish.thredds;

import java.util.Properties;

import org.springframework.util.StringUtils;

import esg.search.core.Record;
import esg.search.publish.api.MetadataEnhancer;

/**
 * Class that enhances the published metadata from a static properties file.
 * Specifically, the given metadata keys are looked up in the properties file,
 * and if found the corresponding values are added to the metadata.
 * 
 * @author Luca Cinquini
 *
 */
public class PropertiesMetadataEnhancer implements MetadataEnhancer {
    
    private final Properties properties;
        
    /**
     * Constructor
     * @param properties : the static properties file that contains additional metadata to be added to the records.
     */
    public PropertiesMetadataEnhancer(final Properties properties) {
        this.properties = properties;
    }

    /**
     * This method implementation uses the given name as the lookup key in the properties file.
     */
    @Override
    public void enhance(final String name, final String value, final Record record) {
                
        if (StringUtils.hasText(properties.getProperty(name))) {
            record.addField(name, properties.getProperty(name));
        }
                
    }

}
