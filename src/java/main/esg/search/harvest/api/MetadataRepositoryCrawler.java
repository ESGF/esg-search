package esg.search.harvest.api;

import java.net.URI;

/**
 * API for crawling a metadata repository (with optional recursion).
 */
public interface MetadataRepositoryCrawler {
	
	/**
	 * Method to crawl the metadata repository available at some URI,
	 * and optionally follow the symbolic links encountered while crawling.
	 * @param uri : the starting URI of metadata repository
	 * @param recursive : true to recursively crawl the locations referenced by the starting location
	 */
	public void crawl(URI uri, boolean recursive) throws Exception;

}
