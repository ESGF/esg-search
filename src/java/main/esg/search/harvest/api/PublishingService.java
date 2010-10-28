package esg.search.harvest.api;

import java.util.List;

/**
 * High level API for publishing and unpublishing metadata into the search engine storage.
 * @author luca.cinquini
 *
 */
public interface PublishingService {

	/**
	 * Method to publish metadata from a remote metadata repository.
	 * @param uri : the location of the remote metadata repository.
	 * @param recursive : true to recursively crawl the remote metadata repository.
	 * @param metadataRepositoryType : the metadata repository type.
	 * @throws Exception
	 */
	void publish(String uri, boolean recursive, MetadataRepositoryType metadataRepositoryType) throws Exception;
	
	/**
	 * Method to unpublish metadata from a remote metadata repository.
	 * @param uri : the location of the remote metadata repository.
	 * @param recursive : true to recursively crawl the remote metadata repository.
	 * @param metadataRepositoryType : the metadata repository type.
	 * @throws Exception
	 */
	void unpublish(String uri, boolean recursive, MetadataRepositoryType metadataRepositoryType) throws Exception;
	
	/**
	 * Method to unpublish a list of records with known identifiers.
	 * @param ids
	 * @throws Exception
	 */
	void unpublish(List<String> ids) throws Exception;
	
}
