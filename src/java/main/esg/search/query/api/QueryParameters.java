package esg.search.query.api;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Class containing query constants.
 * Note: by convention, all query parameters need to be lower case.
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
    public final static String SHARDS = "shards";
    public final static String FROM = "from";
    public final static String TO = "to";
        
    public final static List<String> KEYWORDS = Arrays.asList( new String[]{ OFFSET, LIMIT, QUERY, FORMAT, FACETS, FIELDS, DISTRIB, SHARDS, FROM, TO } );
             
    // standard metadata fields, always included for each result (if available)
    final public static String FIELD_ID = "id";
    final public static String FIELD_TYPE = "type";
    final public static String FIELD_REPLICA = "replica";
    final public static String FIELD_LATEST = "latest";
    final public static String FIELD_MASTER_ID = "master_id";
    final public static String FIELD_INSTANCE_ID = "instance_id";
    final public static String FIELD_DRS_ID = "drs_id";
    final public static String FIELD_TITLE = "title";
    final public static String FIELD_DESCRIPTION = "description";
    final public static String FIELD_TIMESTAMP = "timestamp";
    final public static String FIELD_URL = "url";
    final public static String FIELD_XLINK = "xlink";
    final public static String FIELD_SIZE = "size";
    final public static String FIELD_DATASET_ID = "dataset_id"; // note: generic notation to allow parent-child relations beyond just datasets   
    //final public static String FIELD_FILE_ID = "file_id";
    final public static String FIELD_VERSION = "version";
    final public static String FIELD_CHECKSUM = "checksum";
    final public static String FIELD_CHECKSUM_TYPE = "checksum_type";
    final public static String FIELD_INDEX_NODE = "index_node";
    final public static String FIELD_DATA_NODE = "data_node";
        
    // special query fields for open search geo extension
    public final static String FIELD_BBOX ="bbox";  // west, south, east, north
    public final static String FIELD_LAT ="lat";
    public final static String FIELD_LON = "lon";
    public final static String FIELD_LOCATION = "location";
    public final static String FIELD_RADIUS = "radius";
    public final static String FIELD_POLYGON = "polygon";
    
    // special query fields for open search time extension
    public final static String FIELD_START = "start";
    public final static String FIELD_END = "end";
    
    // metadata field names
    final public static String FIELD_EXPERIMENT = "experiment";
    final public static String FIELD_EXPERIMENT_FAMILY = "experiment_family";
    final public static String FIELD_NORTH = "north_degrees";
    final public static String FIELD_SOUTH = "south_degrees";
    final public static String FIELD_EAST = "east_degrees";
    final public static String FIELD_WEST = "west_degrees";
    final public static String FIELD_DATETIME_START = "datetime_start";
    final public static String FIELD_DATETIME_STOP = "datetime_stop";
    final public static String FIELD_METADATA_FORMAT = "metadata_format";
    final public static String FIELD_METADATA_URL = "metadata_url";
    final public static String FIELD_METADATA_FILE_NAME = "metadata_file_name";
    final public static String FIELD_PROJECT = "project";
    final public static String FIELD_DATA_FORMAT = "data_format";
    final public static String FIELD_INSTRUMENT = "instrument";
    final public static String FIELD_VARIABLE = "variable";
    final public static String FIELD_CF_STANDARD_NAME = "cf_standard_name";
    final public static String FIELD_VARIABLE_LONG_NAME = "variable_long_name";
    final public static String FIELD_GCMD_TERM = "gcmd_term";
    
    // fields that are always allowed in queries, in addition to configured facets
    public final static List<String> CORE_QUERY_FIELDS = Arrays.asList( new String[]{ 
            FIELD_ID, FIELD_TYPE, FIELD_REPLICA, FIELD_LATEST, FIELD_MASTER_ID, FIELD_INSTANCE_ID, FIELD_DRS_ID,
            FIELD_TITLE, FIELD_DESCRIPTION, FIELD_TIMESTAMP, FIELD_URL, FIELD_XLINK, FIELD_SIZE, FIELD_DATASET_ID,
            FIELD_VERSION, FIELD_CHECKSUM, FIELD_CHECKSUM_TYPE, FIELD_DATA_NODE, FIELD_INDEX_NODE,
            FIELD_BBOX, FIELD_BBOX, FIELD_LON, FIELD_LON, FIELD_RADIUS, FIELD_POLYGON,
            FIELD_START, FIELD_END });

    
    // HTTP mime types
    public final static String MIME_TYPE_THREDDS = "application/xml+thredds";   
    public final static String MIME_TYPE_NETCDF = "application/netcdf";
    public final static String MIME_TYPE_GRIDFTP = "application/gridftp";
    public final static String MIME_TYPE_FTP = "application/ftp";
    public final static String MIME_TYPE_LAS = "application/las";   
    public final static String MIME_TYPE_HTML = "text/html";
    public final static String MIME_TYPE_GOOGLE_EARTH = "application/vnd.google-earth.kmz";
    public final static String MIME_TYPE_HDF = "application/x-hdf";
    public final static String MIME_TYPE_OPENDAP = "application/opendap";
    public final static String MIME_TYPE_OPENDAP_DODS = "application/opendap-dods";
    public final static String MIME_TYPE_OPENDAP_DAS = "application/opendap-das";
    public final static String MIME_TYPE_OPENDAP_DDS = "application/opendap-dds";
    public final static String MIME_TYPE_OPENDAP_HTML = "application/opendap-html";
    public final static String MIME_TYPE_RSS = "application/rss+xml";
        
    /**
     * List of invalid text characters - anything that is not within square brackets.
     */
    public static Pattern INVALID_CHARACTERS = Pattern.compile(".*[^a-zA-Z0-9_+\\-\\.\\@\\'\\:\\;\\,\\s/()\\*\\\"\\[\\]].*!");
    
    /**
     * Format for temporal queries.
     */
    private static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
    public static final DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
    
    /**
     * Maximum limit on returned results
     */
    public final static int MAX_LIMIT = 10000;
    
    /**
     * Default number of records requested, if not specified.
     */
    public final static int DEFAULT_LIMIT = 100;

    /**
     * Common record types.
     */
    public final static String TYPE_DATASET = "Dataset";
    public final static String TYPE_FILE = "File";

    /**
     * The default results type to search for, if none is specified.
     */
    public final static String DEFAULT_TYPE = TYPE_DATASET;
    
    private QueryParameters() {};

}
