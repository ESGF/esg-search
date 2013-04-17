package esg.search.publish.plugins;

import java.util.List;
import java.util.Properties;

import esg.search.core.Record;

/**
 * Implementation of {@link MetadataEnhancer} 
 * that inserts all (key, value) pairs contained in the give property file
 * @author Luca Cinquini
 *
 */
public class AllPropertiesMetadataEnhancer extends BaseMetadataEnhancerImpl {
    
    private final Properties properties;
    
    public AllPropertiesMetadataEnhancer(Properties properties) {
        this.properties = properties;
    }


    /**
     * NOTE: (name, values) argument are disregarded
     */
    @Override
    public void enhance(String name, List<String> values, Record record) {
        
        System.out.println("enhancing....");
        
        // loop over properties
        for(String key : properties.stringPropertyNames()) {
            System.out.println("key="+key);
            String value = properties.getProperty(key);
            record.addField(key, value);
          }
        
    }

}
