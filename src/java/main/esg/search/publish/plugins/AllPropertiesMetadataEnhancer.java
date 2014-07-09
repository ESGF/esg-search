package esg.search.publish.plugins;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import esg.search.core.Record;

/**
 * Implementation of {@link MetadataEnhancer} 
 * that inserts all (key, value) pairs contained in the given property file.
 * 
 * @author Luca Cinquini
 *
 */
public class AllPropertiesMetadataEnhancer extends BaseMetadataEnhancerImpl {
    
    private final Properties properties;
    
    private final Log LOG = LogFactory.getLog(this.getClass());
    
    public AllPropertiesMetadataEnhancer(Properties properties) {
        this.properties = properties;
    }
    
    public AllPropertiesMetadataEnhancer(String filePath) {
    	
    	LOG.info("Using metadata from properties file: "+filePath);
        this.properties = new Properties();
        
        try {
        	this.properties.load(new FileInputStream(filePath));
        } catch(IOException e) {
        	LOG.warn(e.getMessage());
        }
        
    }


    /**
     * This implementation inserts all (key,value) pairs contained in the property file.
     * The method arguments "name" and "values" are disregarded.
     */
    @Override
    public void enhance(String name, List<String> values, Record record) {
                
        // loop over properties
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            record.addField(key, value);
          }
        
    }

}
