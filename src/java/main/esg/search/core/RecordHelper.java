package esg.search.core;

import java.util.List;

import org.springframework.util.StringUtils;

import esg.search.query.api.QueryParameters;

/**
 * Helper class to encode/decode composite record fields.
 * For example, the "url" field of a record is encoded as a tuple of the form (url, mime/type, description).
 * 
 * Example:
 * 
 * @author luca.cinquini
 *
 */
public class RecordHelper {
	
	final static String CHAR = "|";
	
	/**
	 * Utility method that can be used to create a unique record identifier from the versioned dataset or file id,
	 * and the data node host name.
	 * Example: obs4MIPs.CNES.AVISO.mon.v1|esg-datanode.jpl.nasa.gov
	 * 
	 * @param versionedId
	 * @param hostName
	 * @return
	 */
	public final static String getUniqueRecordId(final String versionedId, final String hostName) {
	    return versionedId + "|" + hostName;
	}
	
	/**
	 * Utility method that joins the parts of a URL tuple (url, mime/type, name) with a delimiting character.
	 * @param url : the URL location
	 * @param mimeType : the URL mime/type
	 * @param description : a descriptive name for the URL endpoint
	 * @return
	 */
	public static String encodeUrlTuple(final String url, final String mimeType, final String description) {
		final StringBuilder tuple = new StringBuilder();
		tuple.append(url).append(CHAR).append(mimeType).append(CHAR).append(description);
		return tuple.toString();
	}
	
	/**
	 * Utility method that splits a tuple in its constituent components based on the delimiter character.
	 * Note that this method does not check the number of parts,
	 * so it can be used for different fields.
	 * 
	 * @return
	 * @throws Exception
	 */
	public static String[] decodeTuple(final String tuple) throws Exception {
		
		final String[] parts = tuple.split("\\|");
		if (parts.length!=3) throw new Exception("Invalid Record URL value: "+tuple);
		return parts;
		
	}
	
	/**
	 * Utility method to select the first URL of a given record that matches a given mime type.
	 * 
	 * @param record
	 * @param mimeType
	 * @return
	 */
	public static String selectUrlByMimeType(final Record record, final String mimeType) throws Exception {
	   
	    // loop over all record URLs
        final List<String> values = record.getFieldValues(QueryParameters.FIELD_URL);
        
        for (String value : values) {
            String[] parts = RecordHelper.decodeTuple(value);
            // return first URL matching the requested mime type
            if (parts[1].equals(mimeType)) return parts[0];
        }
        
        // URL of given mime type not found
	    return null;
	    
	}
	
	/**
	 * Utility method that joins the parts of an xlink tuple (href, title, type) with a delimiting character.
	 * @param href : the xlink URL, must be not null
	 * @param title : the xlink title, may be null
	 * @param type : the xlink type, may be null
	 * @return
	 */
	public static String encodeXlinkTuple(final String href, final String title, final String type) {
	    final StringBuilder tuple = new StringBuilder();
        tuple.append(href)
             .append(CHAR)
             .append( StringUtils.hasText(title) ? title : "" )
             .append(CHAR)
             .append( StringUtils.hasText(type) ? type : "" );
        return tuple.toString();
	}
	

}
