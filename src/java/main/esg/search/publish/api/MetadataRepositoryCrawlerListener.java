package esg.search.publish.api;

/**
 * Interface for listening to crawling events.
 * 
 * @author Luca Cinquini
 *
 */
public interface MetadataRepositoryCrawlerListener {

    /**
     * Method triggered before the crawling starts.
     * @param uri
     */
    void beforeCrawling(String uri);
    
    /**
     * Method triggered after the crawling ends successfully.
     * @param uri
     */
    void afterCrawlingSuccess(String uri);
    
    /**
     * Method triggered after the crawling ends in error.
     * @param uri
     */
    void afterCrawlingError(String uri);
    
}
