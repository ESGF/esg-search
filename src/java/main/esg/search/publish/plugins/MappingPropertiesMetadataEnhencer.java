package esg.search.publish.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import esg.search.core.Record;
import esg.search.utils.PropertiesUtils;

/**
 * Class that transforms a record's metadata by applying the mappings found in a properties file.
 * Note that the property keys are all lower case, 
 * and the field values are converted to lower case before matching occurs.
 * 
 * @author Luca Cinquini
 *
 */
public class MappingPropertiesMetadataEnhencer extends BaseMetadataEnhancerImpl {
    
    private final Properties properties;
    
    private final Log LOG = LogFactory.getLog(this.getClass());
    
    public MappingPropertiesMetadataEnhencer(final String filePath) {
        this.properties = PropertiesUtils.load(filePath);
        if (LOG.isInfoEnabled()) LOG.info("Using properties mapping file: "+filePath);
    }

    @Override
    public void enhance(String name, List<String> values, Record record) {
                
        // replace existing values with mapped values
        List<String> _values = new ArrayList<String>();
        for (String value : values) {
            if (StringUtils.hasText(properties.getProperty(value.toLowerCase()))) {
                _values.add(properties.getProperty(value.toLowerCase()));
            } else {
                _values.add(value);
            }
        }
        record.setField(name.toLowerCase(), _values);
        
    }

}
