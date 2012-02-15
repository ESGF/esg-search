package esg.search.utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;

public class PropertiesUtils {
    
    private final static Log LOG = LogFactory.getLog(PropertiesUtils.class);
    
    /**
     * Method to load a Properties file from either an absolute path, or a relative classpath.
     * @param filepath
     * @return
     */
    public static Properties load(final String filepath) {
        
        final Properties properties = new Properties();
        
        try {
            File file = null;
            if (filepath.startsWith("/")) {
               file = new File(filepath);
            } else {
                file = new ClassPathResource(filepath).getFile();
            }
            FileInputStream in = new FileInputStream(file);
            properties.load(in);
            in.close();           
        } catch(Exception e) {
            LOG.warn("Properties file: "+filepath+" not found, properties not loaded");
        } 
        
        return properties;
        
    }

}
