package esg.search.publish.impl;

import java.net.URI;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import esg.search.publish.api.MetadataDeletionService;
import esg.search.publish.api.MetadataRepositoryCrawlerManager;
import esg.search.publish.api.MetadataRepositoryType;
import esg.search.publish.api.MetadataRetractionService;
import esg.search.publish.api.PublishingException;
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
    
    private final Log LOG = LogFactory.getLog(this.getClass());

    /**
     * Collaborator that deletes records with known identifiers.
     */
    private final MetadataDeletionService recordRemover;
    
    /**
     * Collaborator that retracts records with known identifiers.
     */
    private final MetadataRetractionService recordRetractor;

    @Autowired
    public PublishingServiceImpl(
            final @Qualifier("publisherCrawler") MetadataRepositoryCrawlerManager publisherCrawler,
            final @Qualifier("unpublisherCrawler") MetadataRepositoryCrawlerManager unpublisherCrawler,
            final @Qualifier("recordRemover") MetadataDeletionService recordRemover,
            final @Qualifier("recordRetractor") MetadataRetractionService recordRetractor) {

        this.publisherCrawler = publisherCrawler;
        this.unpublisherCrawler = unpublisherCrawler;
        this.recordRemover = recordRemover;
        this.recordRetractor = recordRetractor;
        
    }

    @Override
	public void publish(String uri, String filter, boolean recursive, MetadataRepositoryType metadataRepositoryType, URI schema) throws PublishingException {
             
        try {
            publisherCrawler.crawl(uri, filter, recursive, metadataRepositoryType, true, schema); // publish=true
        } catch(Exception e) {
            LOG.error(e.getMessage());
            e.printStackTrace();
            throw new PublishingException(e.getMessage());
        }
		
	}

    @Override
    public void unpublish(String uri, String filter, boolean recursive, MetadataRepositoryType metadataRepositoryType) throws PublishingException {

        try {
            unpublisherCrawler.crawl(uri, filter, recursive, metadataRepositoryType, false, null); // publish=false, schema=null
        } catch(Exception e) {
            LOG.error(e.getMessage());
            e.printStackTrace();
            throw new PublishingException(e.getMessage());
        }

    }

    @Override
    public void unpublish(List<String> ids) throws PublishingException {

        try {
            recordRemover.delete(ids);
        } catch(Exception e) {
            LOG.error(e.getMessage());
            e.printStackTrace();
            throw new PublishingException(e.getMessage());
        }

    }
    
    @Override
    public void retract(List<String> ids) throws PublishingException {

        try {
        	recordRetractor.retract(ids);
        } catch(Exception e) {
            LOG.error(e.getMessage());
            e.printStackTrace();
            throw new PublishingException(e.getMessage());
        }

    }

}
