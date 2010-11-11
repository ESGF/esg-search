package esg.search.publish.api;

/**
 * API for publishing and unpublishing metadata, in support of legacy clients.
 * 
 * @author luca.cinquini
 *
 */
public interface LegacyPublishingService {
	
	/**
	 * Method to publish a hierarchy of THREDDS catalogs.
	 * Note: all input parameters are ignored except for the THREDDS URL.
	 * 
	 * @param parentId: ignored, since the underlying model does not constrain datasets in fixed hierarchies.
	 * @param threddsURL: the URL of the root THREDDS catalogs.
	 * @param resursionLevel: ignored (full recursion is always assumed).
	 * @param status: ignored.
	 * @return: the string "SUCCESS" if the operation completed successfully.
	 * 
	 * throws Exception: if the operation did not complete successfully.
	 */
	String createDataset(String parentId, String threddsURL, int resursionLevel, String status) throws Exception;
	
	/**
	 * Method to unpublish a single dataset.
	 * Note: all input parameters are ignored except for the root dataset identifier.
	 * 
	 * @param datasetId: the root dataset identifier (version-independent).
	 * @param recursive: ignored (only one dataset at a time can be unpublished, since dataset are not organized hierarchically).
	 * @param message: ignored.
	 * 
	 * @throws Exception: if the unpublishing operation did not complete successfully.
	 */
	void deleteDataset(String datasetId, boolean recursive, String message) throws Exception;

}
