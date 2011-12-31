package esg.search.publish.thredds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.util.StringUtils;

import esg.search.publish.api.MetadataEnhancer;

/**
 * Class that enhances the published metadata from a static properties file.
 * 
 * @author Luca Cinquini
 *
 */
public class PropertiesMetadataEnhancer implements MetadataEnhancer {
    
    private final Properties properties;
    
    /**
     * Constructor
     * @param properties : the properties object
     */
    public PropertiesMetadataEnhancer(final Properties properties) {
        this.properties = properties;
    }

    /**
     * This method implementation uses the given name as the property key
     */
    @Override
    public Map<String, List<String>> enhance(String name, String value) {
        
        final Map<String, List<String>> metadata = new HashMap<String,List<String>>();
        
        if (StringUtils.hasText(properties.getProperty(name))) {
            List<String> values = new ArrayList<String>();
            values.add(properties.getProperty(name));
            metadata.put(name, values);
        }
        
        return metadata;
        
    }

}
