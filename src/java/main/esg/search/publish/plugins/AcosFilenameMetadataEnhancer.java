package esg.search.publish.plugins;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import esg.search.core.Record;
import esg.search.query.impl.solr.SolrXmlPars;

/**
 * ACOS-specific {@link MetadataEnhancer} 
 * that extracts the data starting date and time from the filename.
 * 
 * @author Luca Cinquini
 *
 */
public class AcosFilenameMetadataEnhancer extends FilenameMetadataEnhancer {
    
    
    private final Log LOG = LogFactory.getLog(this.getClass());
    
    public AcosFilenameMetadataEnhancer(String pattern, List<String> fields) {
    	super(pattern, fields);
    }
    

    /**
     * This implementation matches the first record URL to the configured filename pattern.
     * The method arguments "name" and "values" are disregarded.
     */
    @Override
    public void enhance(String name, List<String> values, Record record) {
    	
    	// parse filename 'acos_L2s_130101_01_Production_v150151_L2s30300_r01_PolB_130225030150.h5' to extract 'yymmdd'='130101'
    	super.enhance(name, values, record);
    	
    	// additional processing
    	try {
    		
    		// retrieve 'yymmdd' from filename		
	    	String DATE_FORMAT = "yyyyMMdd";
	        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
	        df.setTimeZone(TimeZone.getTimeZone("UTC"));
    		String yymmdd = record.getFieldValue("yymmdd");
    		Date startDate = df.parse("20"+yymmdd);
	        
    		// add fields 'datetime_start', 'datetime_stop'
	        Date stopDate = DateUtils.addDays(startDate, 1);
            record.addField(SolrXmlPars.FIELD_DATETIME_START, SolrXmlPars.SOLR_DATE_TIME_FORMATTER.format(startDate));  
            record.addField(SolrXmlPars.FIELD_DATETIME_STOP,  SolrXmlPars.SOLR_DATE_TIME_FORMATTER.format(stopDate));
            
            // remove field 'yymmdd'
            record.deleteField("yymmdd");
	    	
    	} catch(ParseException e) {
    		LOG.warn(e.getMessage());
    	}
        
    }

}
