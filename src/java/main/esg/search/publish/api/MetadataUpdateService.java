package esg.search.publish.api;

import java.util.HashMap;
import java.util.Map;

/**
 * API for bulk-updating existing metadata records.
 * 
 */
public interface MetadataUpdateService {
	
	/**
	 * Method to change/add/remove metadata fields to/from existing records.
	 * @param url: base Solr URL (example: "http://localhost:8984/solr")
	 * @param core: Solr core (example: "datasets")
	 * @param action: one of "set", "add", "remove"
	 * @param metadata: dictionary of queries to map of field name and values to be updated for all matching results
	 *                  example:
	 *                  { 
	 *                    'id:test.test.v1.testData.nc|esgf-dev.jpl.nasa.gov': 
     *          	      {
     *                     'xlink':['http://esg-datanode.jpl.nasa.gov/.../zosTechNote_AVISO_L4_199210-201012.pdf|AVISO Sea Surface Height Technical Note|summary']
     *                    }
     *                  }
	 *
	 * @return: number of documents that matched the query (and therefore updated - across all queries combined)
	 */
	public int update(String url, String core, String action, HashMap<String, Map<String,String[]>> doc) throws Exception;

}
