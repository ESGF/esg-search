package esg.search.query.impl.solr;

/**
 * Class containing parameters for the Solr XML schema.
 */
public class SolrXmlPars {
	
	final public static String ELEMENT_ADD = "add";
	final public static String ELEMENT_DOC = "doc";
	final public static String ELEMENT_FIELD = "field";
	final public static String ELEMENT_LST = "lst";
	final public static String ELEMENT_RESULT = "result";
	final public static String ELEMENT_INT = "int";
	final public static String ELEMENT_STR = "str";
	final public static String ELEMENT_ARR = "arr";
	
	final public static String  ELEMENT_FACET_COUNTS = "facet_counts";
	final public static String  ELEMENT_FACET_FIELDS = "facet_fields";
	
	final public static String ATTRIBUTE_NAME = "name";
	final public static String ATTRIBUTE_NUM_FOUND = "numFound";
	final public static String ATTRIBUTE_START = "start";
	
	final public static String ATTRIBUTE_VALUE_RESPONSE = "response";
	
	// field names: generic search record
    // <field name="id">0</field>
	// <field name="title">Record #0</field>
    // <field name="type">Dataset</field>
    // <field name="frequency">Monthly</field>
    // .........
	final public static String FIELD_ID = "id";
	final public static String FIELD_TITLE = "title";
	final public static String FIELD_NAME = "name";
	final public static String FIELD_DESCRIPTION = "description";
	final public static String FIELD_TYPE = "type";
	final public static String FIELD_URL = "url";
	final public static String FIELD_XLINK = "xlink";
	final public static String FIELD_XREF = "xref";
	
	// field names: physical data
	final public static String FIELD_PROJECT = "project";
	final public static String FIELD_INSTRUMENT = "instrument";
	final public static String FIELD_VARIABLE = "variable";
	final public static String FIELD_CF_VARIABLE = "cf_variable";
	final public static String FIELD_GCMD_VARIABLE = "gcmd_variable";
	
	final public static String FIELD_DATETIME_START = "datetime_start";
	final public static String FIELD_DATETIME_STOP = "datetime_stop";
	
	final public static String FIELD_NORTH = "north_degrees";
	final public static String FIELD_SOUTH = "south_degrees";
	final public static String FIELD_EAST = "east_degrees";
	final public static String FIELD_WEST = "west_degrees";
	
	final public static String FIELD_DATA_FORMAT = "data_format";
	final public static String FIELD_FILE_NAME = "file_name";

	
	/**
	 * Private constructor prevents class instantiation.
	 */
	private SolrXmlPars() {}

}
