package esg.search.publish.plugins;

import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import esg.search.core.Record;
import esg.search.query.api.QueryParameters;

/**
 * Utility class to add xink references to Technical Notes for specific observational files.
 * The xlink references are specified in a properties file of the form:
 * 
 * obs4MIPs.NASA-JPL.MLS.mon.v1.hus_MLS_L3_v03-3x_200408-201012.nc:href|title|type
 * obs4MIPs.NASA-JPL.MLS.mon.v1.ta_MLS_L3_v03-3x_200408-201012.nc:href|title|type
 * 
 * @author Luca Cinquini
 *
 */
public class TechNoteMetadataEnhancer extends BaseMetadataEnhancerImpl {
    
    private final Log LOG = LogFactory.getLog(this.getClass());
    
    final Properties properties = new Properties();
    
    public TechNoteMetadataEnhancer(final String propertiesFilePath) {
        try {
            properties.load(new FileInputStream(propertiesFilePath));
            if (LOG.isInfoEnabled()) LOG.info("Using information for Technical Notes xlinks from file:"+propertiesFilePath);
        } catch(Exception e) {
            LOG.warn("Properties file: "+propertiesFilePath+" not found, skipping");
        }
    }

    @Override
    public void enhance(String name, List<String> values, Record record) {
        
        if (name.equals(QueryParameters.FIELD_MASTER_ID)) {
            for (String value : values) {
                if (StringUtils.hasText(properties.getProperty(value))) {
                    if (LOG.isDebugEnabled()) LOG.debug("Setting Tech Note xlink="+properties.getProperty(value));
                    record.addField(QueryParameters.FIELD_XLINK, properties.getProperty(value));
                }
            }
        }

    }

}
