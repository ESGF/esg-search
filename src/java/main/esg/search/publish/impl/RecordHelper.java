package esg.search.publish.impl;

/**
 * Helper class to encode/decode complex record fields.
 * @author luca.cinquini
 *
 */
public class RecordHelper {
	
	final static String CHAR = "|";
	
	/**
	 * Utility method that joins the parts of a service field value with a delimiting character.
	 * @param serviceType
	 * @param serviceDescription
	 * @param serviceUrl
	 * @return
	 */
	public static String encodeServiceField(final String serviceType, final String serviceDescription, final String serviceUrl) {
		final StringBuilder sb = new StringBuilder();
		sb.append(serviceType).append(CHAR).append(serviceDescription).append(CHAR).append(serviceUrl);
		return sb.toString();
	}
	
	/**
	 * Utility method that parses a service field value in its constituent components.
	 * @param serviceField
	 * @return
	 * @throws Exception
	 */
	public static String[] decodeServiceField(final String serviceField) throws Exception {
		
		final String[] parts = serviceField.split("\\|");
		if (parts.length!=3) throw new Exception("Invalid Record service value: "+serviceField);
		return parts;
		
	}

}
