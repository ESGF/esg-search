package esg.search.query.api;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Class containing query constants.
 * 
 * @author Luca Cinquini
 *
 */
public class QueryParameters {
        
    // reserved query keywords
    public final static String OFFSET = "offset";
    public final static String LIMIT = "limit";
    public final static String QUERY = "query";
    public final static String FORMAT = "format";
    public final static String FACETS = "facets";
    public final static String FIELDS = "fields";
    public final static String DISTRIB = "distrib";
    
    // open search geo extension
    public final static String LAT ="lat";
    public final static String LON = "lon";
    public final static String LOCATION = "location";
    public final static String RADIUS = "radius";
    public final static String POLYGON = "polygon";
    
    // open search time extension
    public final static String START = "start";
    public final static String END = "end";
          
    // record special fields
    public final static String ID = "id";
    public final static String TYPE = "type";
    public final static String FROM = "from";
    public final static String TO = "to";


    public final static List<String> KEYWORDS = Arrays.asList( new String[]{ OFFSET, LIMIT, QUERY, FORMAT, FACETS, FIELDS, DISTRIB,
                                                                             LAT, LON, LOCATION, RADIUS, POLYGON,
                                                                             START, END,
                                                                             ID, TYPE, FROM, TO } );
    
    /**
     * List of invalid text characters - anything that is not within square brackets.
     */
    public static Pattern INVALID_CHARACTERS = Pattern.compile(".*[^a-zA-Z0-9_+\\-\\.\\@\\'\\:\\;\\,\\s/()\\*\\\"].*");


    
    private QueryParameters() {};

}
