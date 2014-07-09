package esg.search.publish.plugins;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import esg.search.core.Record;
import esg.search.query.api.QueryParameters;
import esg.search.query.impl.solr.SolrXmlPars;

/**
 * Implementation of {@link MetadataEnhancer} that extracts metadata from the filename.
 * 
 * @author Luca Cinquini
 *
 */
public class FilenameMetadataEnhancer extends BaseMetadataEnhancerImpl {
    
	// pattern matching the filename
    Pattern pattern = null;
    
    // list of group names contained in the pattern
    List<String> fields = new ArrayList<String>();
    
    private final Log LOG = LogFactory.getLog(this.getClass());
    
    public FilenameMetadataEnhancer(String pattern, List<String> fields) {
    	
    	this.pattern = Pattern.compile(pattern); 
    	
    	this.fields = fields;
    }
    

    /**
     * This implementation matches the first record URL to the configured filename pattern.
     * The method arguments "name" and "values" are disregarded.
     */
    @Override
    public void enhance(String name, List<String> values, Record record) {
                
        // loop over record access URLs
        for (String url : record.getFieldValues(QueryParameters.FIELD_URL)) {
        	
        	String[] parts = url.split("\\|");
        	String fileName = parts[0].substring(parts[0].lastIndexOf("/")+1);
        	if (LOG.isTraceEnabled()) LOG.debug("Matching filename="+fileName+" to regular expression="+this.pattern.toString());
        		Matcher matcher = pattern.matcher(fileName);
        		if (matcher.find()) {
        			int count = matcher.groupCount();
        			
        			// NOTE: count=0 is the full text match
        		    for (int i=1; i<count+1; i++) {
        		    	
        		    	String fieldName = this.fields.get(i-1);
        		    	String fieldValue = matcher.group(i);
        		    	
        		    	record.addField(fieldName, fieldValue);
        		    	return; // return at first match

        		    }
        		}
          }
        
    }

}
