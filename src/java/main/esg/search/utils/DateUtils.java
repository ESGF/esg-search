package esg.search.utils;

import java.text.ParseException;
import java.util.Date;

/**
 * Utility class to parse date strings according to a set of recognized patterns
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

}
