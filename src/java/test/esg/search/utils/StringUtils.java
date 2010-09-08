package esg.search.utils;

public class StringUtils {
	
	/**
	 * Method to remove all newline and blank characters from a string.
	 * @param s
	 * @return
	 */
	public static String compact(final String s) {
		return s.replaceAll("\n", "").replaceAll("\\s+", "");
	}

}
