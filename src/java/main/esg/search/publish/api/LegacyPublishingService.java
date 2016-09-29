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
	 * @return: the string "SUCCESSFUL" if the operation completed successfully.
	 * 
	 * throws PublishingException: if the operation did not complete successfully.
	 */
	String createDataset(String parentId, String threddsURL, int resursionLevel, String status) throws PublishingException;
	
	/**
	 * Method to unpublish a single dataset.
	 * Note: all input parameters are ignored except for the root dataset identifier,
	 * which is matched against both the "master_id" and "instance_id" of all records in the local index.
	 * 
	 * @param datasetId: the root dataset identifier (version-independent).
	 * @param recursive: ignored (only one dataset at a time can be unpublished, since dataset are not organized hierarchically).
	 * @param message: ignored.
	 * 
	 * @throws PublishingException: if the unpublishing operation did not complete successfully.
	 */
	void deleteDataset(String datasetId, boolean recursive, String message) throws PublishingException;
	
	/**
	 * Method to retract a single dataset i.e. delete all the associated files abd aggregations,
	 * but keep the dataset record and mark it with "retracted=true" (and "latest=false").
	 * 
	 * @param datasetId
	 * @param recursive
	 * @param message
	 * @throws PublishingException
	 */
	void retractDataset(String datasetId, boolean recursive, String message) throws PublishingException;
	
    /**
     * Legacy method to check for the status of a current (asynchronous) ongoing publishing operation.
     * This method is only meant to be implemented to support clients of the legacy asynchronous API.
     * 
     * @param operationHandle: the publishing operation identifier (ignored)
     *
     * @return always "SUCCESSFUL".
     */
	String getPublishingStatus(final String operationHandle) throws PublishingException;

}
