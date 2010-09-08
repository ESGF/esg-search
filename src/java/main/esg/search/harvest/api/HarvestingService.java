package esg.search.harvest.api;

import java.net.URI;


/**
 * Service for harvesting search records from different remote metadata repositories.
 */
public interface HarvestingService {

	/**
	 * 
	 * @param uri
	 * @param recursive
	 * @param metadataRepositoryType
	 * @throws Exception
	 */
	void harvest(URI uri, boolean recursive, MetadataRepositoryType metadataRepositoryType) throws Exception;

}