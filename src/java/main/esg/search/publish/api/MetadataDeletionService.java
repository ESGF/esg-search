package esg.search.publish.api;

import java.util.List;

/**
 * Low-level API for removing metadata records from the system.
 * 
 * @author luca.cinquini
 *
 */
public interface MetadataDeletionService  {


	/**
	 * Method to delete a list of records with given identifiers.
	 * @param ids
	 */
	void delete(List<String> ids) throws Exception;
	
}
