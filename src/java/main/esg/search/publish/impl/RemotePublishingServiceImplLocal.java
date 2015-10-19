package esg.search.publish.impl;

import org.springframework.stereotype.Service;

import esg.search.publish.api.PublishingService;
import esg.search.query.api.SearchService;
import esg.search.utils.ApplicationContextProvider;

/**
 * Class that allows Hessian invocation of the {@link PublishingService} 
 * configured for publishing to the local Solr index.
 * This class behaves exatctly like {@link RemotePublishingServiceImpl} 
 * except that it uses Spring beans configured for the local Solr index. 
 * 
 * @author Luca Cinquini
 *
 */
@Service("remotePublishingServiceLocal")
public class RemotePublishingServiceImplLocal extends RemotePublishingServiceImpl {
    
    // Service that publishes to the local Solr index.
    private final static String PUBLISHING_SERVICE_BEAN_LOCAL = "securePublishingServiceLocal";
    
    // Service that searches the local Solr index.
    private final static String SEARCH_SERVICE_BEAN_LOCAL = "searchServiceLocal";
        
    /**
     * @Override
     * Retrieves the configured {@link PublishingService} from the Spring application context.
     * @return
     */
    @Override
    protected PublishingService getPublishingService() {
        return ApplicationContextProvider.getApplicationContext().getBean(PUBLISHING_SERVICE_BEAN_LOCAL, PublishingService.class);
    }
    
    /**
     * @Override
     * Retrieves the configured {@link SearchService} from the Spring application context.
     * @return
     */
    @Override
    protected SearchService getSearchService() {
        return ApplicationContextProvider.getApplicationContext().getBean(SEARCH_SERVICE_BEAN_LOCAL, SearchService.class);
    }
    
}