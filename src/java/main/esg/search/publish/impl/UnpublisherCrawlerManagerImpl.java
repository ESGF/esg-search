package esg.search.publish.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import esg.search.publish.api.MetadataRepositoryCrawler;
import esg.search.publish.api.RecordConsumer;

/**
 * Subclass of {@link MetadataRepositoryCrawlerManagerImpl} configured for unpublishing.
 * 
 * @author luca.cinquini
 */
@Component
public class UnpublisherCrawlerManagerImpl extends MetadataRepositoryCrawlerManagerImpl {
	
	@Autowired
	public UnpublisherCrawlerManagerImpl(final MetadataRepositoryCrawler[] _crawlers, 
			                            final @Qualifier("scrabber") RecordConsumer indexer) {
		super(_crawlers);
		this.subscribe(indexer);
	}

}
