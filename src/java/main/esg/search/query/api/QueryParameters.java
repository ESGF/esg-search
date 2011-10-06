package esg.search.query.api;

import java.util.Arrays;
import java.util.List;

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
    public final static String BACK = "back"; 
    public final static String TYPE = "type";
      
    
    // HTTP parameters that are interpreted specially because they enter into special queries
    public final static String ID = "id";
    public final static String FROM = "from";
    public final static String TO = "to";
    
    public final static List<String> KEYWORDS = Arrays.asList( new String[]{ OFFSET, LIMIT, QUERY, BACK, TYPE, ID, FROM, TO } );
    
    private QueryParameters() {};

}
