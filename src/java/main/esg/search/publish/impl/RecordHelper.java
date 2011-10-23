package esg.search.publish.impl;

/**
 * Helper class to encode/decode URL tuples of the form (url, mime/type, description).
 * 
 * Example:
 * 
 * @author luca.cinquini
 *
 */
public class RecordHelper {
	
	final static String CHAR = "|";
	
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
	 * Utility method that parses a URL tuple in its constituent components.
	 * 
	 * @param serviceField
	 * @return
	 * @throws Exception
	 */
	public static String[] decodeUrlTuple(final String tuple) throws Exception {
		
		final String[] parts = tuple.split("\\|");
		if (parts.length!=3) throw new Exception("Invalid Record URL value: "+tuple);
		return parts;
		
	}
	

}
