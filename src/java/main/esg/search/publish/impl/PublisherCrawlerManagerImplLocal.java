package esg.search.publish.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import esg.search.publish.api.MetadataRepositoryCrawler;
import esg.search.publish.api.RecordConsumer;

/**
 * Subclass of {@link MetadataRepositoryCrawlerManagerImpl} configured for publishing to the local Solr index.
 * 
 * @author luca.cinquini
 */
@Component("publisherCrawlerLocal")
public class PublisherCrawlerManagerImplLocal extends MetadataRepositoryCrawlerManagerImpl {
	
	@Autowired
	public PublisherCrawlerManagerImplLocal(final MetadataRepositoryCrawler[] _crawlers, 
			                            final @Qualifier("indexerLocal") RecordConsumer indexer) {
		super(_crawlers);
		this.subscribe(indexer);
	}

}
