package esg.search.publish.thredds;

import java.util.List;
import java.util.Properties;

import org.springframework.util.StringUtils;

import esg.search.core.Record;
import esg.search.publish.api.MetadataEnhancer;

/**
 * Class that enhances the published metadata from a static properties file.
 * 
 * @author Luca Cinquini
 *
 */
public class PropertiesMetadataEnhancer implements MetadataEnhancer {
    
    private final Properties properties;
    
    private final String key;
        
    /**
     * Constructor
     * @param properties : the static properties file that contains additional metadata to be added to the records.
     */
    public PropertiesMetadataEnhancer(final String key, final Properties properties) {
        this.key = key;
        this.properties = properties;
    }

    /**
     * This method implementation completely disregards the supplied (name, values) 
     * and uses the instance key as lookup into the properties file.
     */
    @Override
    public void enhance(final String name, final List<String> values, final Record record) {
                
        if (StringUtils.hasText(properties.getProperty(key))) {
            record.addField(key, properties.getProperty(key));
        }
                
    }

}
