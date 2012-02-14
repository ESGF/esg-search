package esg.search.publish.impl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import esg.search.publish.api.MetadataRepositoryCrawlerListener;

/**
 * Class that logs the status of a metadata crawling operation to a log file.
 * 
 * @author Luca Cinquini
 *
 */
public class FileLogger implements MetadataRepositoryCrawlerListener {
    
    private java.io.FileWriter logFile = null;
    
    protected final Log LOG = LogFactory.getLog(this.getClass());
    final private static String NEWLINE = System.getProperty("line.separator");
    
    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private final static String STATUS_SUCCESS = "OK";
    private final static String STATUS_ERROR = "ERROR";
    
    public FileLogger(final String filepath) throws Exception {
        logFile = new java.io.FileWriter(filepath, false); // append=false
    }

    @Override
    public void beforeCrawling(String uri) {
        // do nothing
    }

    @Override
    public void afterCrawlingSuccess(String uri) {
        write(uri, STATUS_SUCCESS);
    }
    
    @Override
    public void afterCrawlingError(String uri) {
       write(uri, STATUS_ERROR);
    }

    public void destroy() {
        try {
            if (logFile!=null) logFile.close();
        } catch (IOException e) {
            LOG.warn(e.getMessage());
        }
    }
    
    private void write(String uri, String status) {
        try {
            String[] parts = new String[] { DATE_FORMAT.format(new Date()), uri, status };
            logFile.write(StringUtils.join(Arrays.asList(parts), ",") + NEWLINE);
            logFile.flush();
        } catch(IOException e) {
            LOG.warn(e.getMessage());
        }
    }

}
