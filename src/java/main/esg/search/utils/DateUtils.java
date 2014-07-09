package esg.search.utils;

import java.text.ParseException;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import esg.search.query.impl.solr.SolrXmlPars;

/**
 * Utility class to parse date strings according to a set of recognized patterns.
 * 
 * @author Luca Cinquini
 *
 */
public class DateUtils {
    
    private final static String[] patterns = new String[] {"yyyy-MM-dd'T'HH:mm:ssZ",
                                                           "yyyy-MM-dd HH:mm:ss",
                                                           "yyyyMMdd HH:mm:ss" };
    
    public final static Date parse(String date) throws ParseException {
        
        // must compensate for common DateUtils NOT been able to parse ISO8601
        if (date.endsWith("Z")) {
            date = date.replaceAll("Z$", "GMT+00:00");
        }
        return org.apache.commons.lang.time.DateUtils.parseDate(date, patterns);
        
    }
    
    /**
     * Returns a date-time string formatted for ingestion into Solr.
     * 
     * @param offset: offset date-time string in the form "1993-01-01T00:00:00Z"
     * @param seconds: number of seconds from offset date-time
     * 
     * @param offset
     * @param seconds
     * @return
     */
    public final static String toSolrDateTimeFormat(String offset, double seconds) {
    	
		// initialize DateTimeFormatter
	    DateTimeZone timeZone = DateTimeZone.forID( "Zulu" );
	    DateTime dateTimeStart = new DateTime( offset, timeZone );
	    DateTime dateTimeUtcGmt = dateTimeStart.withZone( DateTimeZone.UTC );
	    DateTimeFormatter formatter = DateTimeFormat.forPattern( SolrXmlPars.SOLR_DATE_FORMAT ).withZone( timeZone );

	    // sum seconds to offset, convert to date
		DateTime dt = dateTimeUtcGmt.plusSeconds( (int)seconds );
	    //String iso8601String = dt.toString();
	    return formatter.print(dt);
    	
    }

}
