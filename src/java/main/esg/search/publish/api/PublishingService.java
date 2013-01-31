package esg.search.publish.api;

import java.net.URI;
import java.util.List;

/**
 * High level API for publishing and unpublishing metadata into the search engine storage.
 * 
 * @author luca.cinquini
 *
 */
public interface PublishingService {

	/**
	 * Method to publish metadata from a remote metadata repository.
	 * @param uri : the location of the remote metadata repository.
	 * @param filter: optional regex to filter the repository URIs
	 * @param recursive : true to recursively crawl the remote metadata repository.
	 * @param metadataRepositoryType : the metadata repository type.
	 * @param schema : optional schema for record validation
	 * @throws PublishingException
	 */
	void publish(String uri, String filter, boolean recursive, MetadataRepositoryType metadataRepositoryType, URI schema) throws PublishingException;
	
	/**
	 * Method to unpublish metadata from a remote metadata repository.
	 * @param uri : the location of the remote metadata repository.
	 * @param filter: optional regex to filter the repository URIs
	 * @param recursive : true to recursively crawl the remote metadata repository.
	 * @param metadataRepositoryType : the metadata repository type.
	 * @throws PublishingException
	 */
	void unpublish(String uri, String filter, boolean recursive, MetadataRepositoryType metadataRepositoryType) throws PublishingException;
	
	/**
	 * Method to unpublish a list of records with known identifiers.
	 * @param ids
	 * @throws PublishingException
	 */
	void unpublish(List<String> ids) throws PublishingException;
	
}
