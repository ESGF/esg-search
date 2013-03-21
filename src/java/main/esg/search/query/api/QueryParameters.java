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
    public final static String SHARDS = "shards";
    public final static String FROM = "from";
    public final static String TO = "to";
    public final static String SORT = "sort";
        
    public final static List<String> KEYWORDS = Arrays.asList( new String[]{ OFFSET, LIMIT, QUERY, FORMAT, FACETS, FIELDS, DISTRIB, SHARDS, FROM, TO, SORT } );
             
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
    final public static String FIELD_ACCESS = "access";
    final public static String FIELD_XLINK = "xlink";
    final public static String FIELD_SIZE = "size";
    final public static String FIELD_DATASET_ID = "dataset_id"; // note: generic notation to allow parent-child relations beyond just datasets   
    final public static String FIELD_TRACKING_ID = "tracking_id";
    final public static String FIELD_VERSION = "version";
    final public static String FIELD_CHECKSUM = "checksum";
    final public static String FIELD_CHECKSUM_TYPE = "checksum_type";
    final public static String FIELD_INDEX_NODE = "index_node";
    final public static String FIELD_DATA_NODE = "data_node";
    final public static String FIELD_NUMBER_OF_FILES = "number_of_files";
    final public static String FIELD_NUMBER_OF_AGGREGATIONS = "number_of_aggregations";
            
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
    
    //special query fields for the wget scirpt generator
    public final static String FIELD_WGET_PATH = "download_structure";
    public static final String FIELD_WGET_EMPTYPATH = "download_emptypath";
    
    // fields that are always allowed in queries, in addition to configured facets
    public final static List<String> CORE_QUERY_FIELDS = Arrays.asList( new String[]{ 
            FIELD_ID, FIELD_TYPE, FIELD_REPLICA, FIELD_LATEST, FIELD_MASTER_ID, FIELD_INSTANCE_ID, FIELD_DRS_ID,
            FIELD_TITLE, FIELD_DESCRIPTION, FIELD_TIMESTAMP, FIELD_URL, FIELD_XLINK, FIELD_SIZE, 
            FIELD_NUMBER_OF_FILES, FIELD_NUMBER_OF_AGGREGATIONS, FIELD_DATASET_ID, FIELD_TRACKING_ID, FIELD_ACCESS,
            FIELD_VERSION, FIELD_CHECKSUM, FIELD_CHECKSUM_TYPE, FIELD_DATA_NODE, FIELD_INDEX_NODE,
            FIELD_BBOX, FIELD_LAT, FIELD_LON, FIELD_RADIUS, FIELD_POLYGON,
            FIELD_START, FIELD_END,
            FIELD_WGET_PATH, FIELD_WGET_EMPTYPATH});

    
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
    // recognized date/time format for input metadata records
    //public static String[] DATE_PATTERNS = new String[] { "yyyy-MM-dd'T'HH:mm:ssZ", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd",
    //                                                      "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd" };                                                     
    // required date/time format for Solr documents
    //public static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    //public static final DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        
    /**
     * Default number of records requested, if not specified.
     */
    public final static int DEFAULT_LIMIT = 100;
    
    /**
     * Large default limit for wget scripts.
     */
    public final static int LARGE_LIMIT = 1000;
    
    /**
     * Maximum limit on returned results
     */
    public final static int MAX_LIMIT = 10000;

    /**
     * Common record types.
     */
    public final static String TYPE_DATASET = "Dataset";
    public final static String TYPE_FILE = "File";
    public final static String TYPE_AGGREGATION = "Aggregation";

    /**
     * The default results type to search for, if none is specified.
     */
    public final static String DEFAULT_TYPE = TYPE_DATASET;
    
    /**
     * Special value for crawling filter to process ALL catalogs (i.e. no filter is applied).
     */
    public final static String ALL = "ALL";
    
    /*
     * Timeout default values (in milliseconds) and properties for changing them.
     */
    // 1 second connection timeout
    public final static int DEFAULT_CONNECTION_TIMEOUT = 1000;    
    public final static String PROPERTY_CONNECTION_TIMEOUT = "index.timeout.connection";
    // 10 seconds read timeout for datasets
    public final static int DEFAULT_DATASETS_READ_TIMEOUT = 10000;
    public final static String PROPERTY_DATASETS_READ_TIMEOUT = "index.timeout.read.datasets";
    // 1 minute read timeout for files
    public final static int DEFAULT_FILES_READ_TIMEOUT = 3600000;
    public final static String PROPERTY_FILES_READ_TIMEOUT = "index.timeout.read.files";
    
    // schema URIs
    public final static String SCHEMA_ESGF = "esgf";
    public final static String SCHEMA_GEO = "geo";
    
    // other special fields
    public final static String PROJECT = "project";
    
    // key for looking up schema root location from /esgf/config/esgf.properties
    public final static String SCHEMA_LOCATION_PROPERTY = "esgf.publisher.resources.home";
    
    // default schema location, if property is not specified
    public final static String SCHEMA_DEFAULT_LOCATION = "/esg/config/"; 
    
    // /esg/config/esgf.properties: esgf.host=....
    public final static String HOSTNAME_PROPERTY = "esgf.host";

    
    private QueryParameters() {};

}
