package esg.search.publish.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.saml2.core.Action;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;

import esg.orp.app.Authorizer;
import esg.search.publish.api.MetadataDeletionService;
import esg.search.publish.api.MetadataRepositoryCrawlerManager;
import esg.search.publish.api.MetadataRepositoryType;
import esg.search.publish.api.PublishingService;

/**
 * Implementation of {@link PublishingService} that delegates all functionality
 * to collaborating beans for crawling remote metadata repositories, producing
 * search records, and consuming search records for ingestion or removal.
 * 
 * @author luca.cinquini
 * 
 */
@Service("publishingService")
public class PublishingServiceImpl implements PublishingService {

    /**
     * Collaborator that crawls remote metadata repositories for the purpose of
     * publishing records into the system.
     */
    private final MetadataRepositoryCrawlerManager publisherCrawler;

    /**
     * Collaborator that crawls remote metadata repositories for the purpose of
     * unpublishing records from the system.
     */
    private final MetadataRepositoryCrawlerManager unpublisherCrawler;

    /**
     * Collaborator that deletes records with known identifiers.
     */
    private final MetadataDeletionService recordRemover;

    /**
     * Optional collaborator used to secure publishing calls. 
     * If none is configured, no authorization will take place.
     */
    private Authorizer authorizer = null;
    
    private final Log LOG = LogFactory.getLog(this.getClass());

    @Autowired
    public PublishingServiceImpl(
            final PublisherCrawlerManagerImpl publisherCrawler,
            final UnpublisherCrawlerManagerImpl unpublisherCrawler,
            final MetadataDeletionService recordRemover) {

        this.publisherCrawler = publisherCrawler;
        this.unpublisherCrawler = unpublisherCrawler;
        this.recordRemover = recordRemover;
    }

    @Autowired
    public void setAuthorizer(Authorizer authorizer) {
        this.authorizer = authorizer;
    }

    @Override
	public void publish(String uri, boolean recursive, MetadataRepositoryType metadataRepositoryType) throws Exception {
               
        checkAuthorization(uri);
		publisherCrawler.crawl(uri, recursive, metadataRepositoryType, true); // publish=true
		
	}

    @Override
    public void unpublish(String uri, boolean recursive,
            MetadataRepositoryType metadataRepositoryType) throws Exception {

        checkAuthorization(uri);
        unpublisherCrawler.crawl(uri, recursive, metadataRepositoryType, false); // publish=false

    }

    @Override
    public void unpublish(List<String> ids) throws Exception {

        //checkAuthorization(ids);
        recordRemover.delete(ids);

    }
    
    /**
     * Method to check that the user is authorized to execute a publishing/unpublishing operation.
     * @param uri
     * @throws Exception
     */
    private void checkAuthorization(String uri) throws Exception {
        
        if (authorizer!=null) {
            
            boolean authorized = false;
            String openid = "";
            final SecurityContext secCtx = SecurityContextHolder.getContext();
            final Authentication auth = secCtx.getAuthentication();
            if (LOG.isDebugEnabled()) LOG.debug("URL="+uri+" Security context authentication="+auth);

            if (auth!=null && auth instanceof PreAuthenticatedAuthenticationToken) {
                
                openid = auth.getName();
                if (LOG.isDebugEnabled()) LOG.debug("User is authenticated, openid="+openid);

                authorized = authorizer.authorize(openid, uri, Action.WRITE_ACTION);

            }
            
            // throw exception is user is not authorized
            if (!authorized) throw new Exception("User: "+openid+" is not authorized to publish/unpublish resource: "+uri);
            
        }

    }

}
