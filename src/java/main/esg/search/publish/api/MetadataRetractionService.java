package esg.search.publish.api;

import java.util.List;

/**
 * Low-level API for "retracting" metadata records in the system.
 * 
 * @author luca.cinquini
 *
 */
public interface MetadataRetractionService  {


	/**
	 * Method to retract a list of records with given identifiers.
	 * @param ids
	 */
	void retract(List<String> ids) throws Exception;
	
}
