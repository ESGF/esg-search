package esg.search.publish.api;

import java.rmi.RemoteException;
import java.util.List;

/**
 * High level API for publishing and unpublishing metadata into the search engine storage.
 * 
 * All methods throw RemoteExceptions, which are correctly
 * propagated to the client through the Hessian protocol by the Spring framework.
 * 
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
	void publish(String uri, boolean recursive, MetadataRepositoryType metadataRepositoryType) throws RemoteException;
	
	/**
	 * Method to unpublish metadata from a remote metadata repository.
	 * @param uri : the location of the remote metadata repository.
	 * @param recursive : true to recursively crawl the remote metadata repository.
	 * @param metadataRepositoryType : the metadata repository type.
	 * @throws Exception
	 */
	void unpublish(String uri, boolean recursive, MetadataRepositoryType metadataRepositoryType) throws RemoteException;
	
	/**
	 * Method to unpublish a list of records with known identifiers.
	 * @param ids
	 * @throws Exception
	 */
	void unpublish(List<String> ids) throws RemoteException;
	
}
