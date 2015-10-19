package esg.search.publish.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import esg.search.publish.api.MetadataDeletionService;
import esg.search.publish.api.PublishingService;

/**
 * Implementation of {@link PublishingService} that publishes/unpublishes records from the local Solr index.
 * 
 * @author luca.cinquini
 * 
 */
@Service("publishingServiceLocal")
public class PublishingServiceImplLocal extends PublishingServiceImpl {

    @Autowired
    public PublishingServiceImplLocal(
            final @Qualifier("publisherCrawlerLocal") PublisherCrawlerManagerImpl publisherCrawler,
            final @Qualifier("unpublisherCrawlerLocal") UnpublisherCrawlerManagerImpl unpublisherCrawler,
            final @Qualifier("recordRemoverLocal") MetadataDeletionService recordRemover) {


    	super(publisherCrawler, unpublisherCrawler, recordRemover);

    }

}
