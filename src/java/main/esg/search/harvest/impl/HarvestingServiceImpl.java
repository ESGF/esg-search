package esg.search.harvest.impl;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;

import esg.search.harvest.api.HarvestingService;
import esg.search.harvest.api.MetadataRepositoryCrawler;
import esg.search.harvest.api.MetadataRepositoryType;
import esg.search.harvest.api.RecordConsumer;
import esg.search.harvest.api.RecordProducer;

/**
 * Service class that manages the harvesting of search records from different remote metadata repositories.
 */
public class HarvestingServiceImpl implements HarvestingService {
	
	final Map<MetadataRepositoryType, MetadataHarvester> harvesters;
	
	final List<RecordConsumer> consumers;
	
	public HarvestingServiceImpl(final Map<MetadataRepositoryType, MetadataHarvester> harvesters, final List<RecordConsumer> consumers) {
		
		this.harvesters = harvesters;
		this.consumers = consumers;
		
		// subscribe record consumers to record producers
		for (final RecordProducer producer : harvesters.values()) {
			for (final RecordConsumer consumer : consumers) {
				producer.subscribe(consumer);
			}
		}
		
	}
	
	/* (non-Javadoc)
	 * @see esg.search.harvest.HarvestingService#harvest(java.net.URI, boolean, esg.search.harvest.MetadataRepositoryType)
	 */
	public void harvest(final URI uri, boolean recursive, final MetadataRepositoryType metadataRepositoryType) throws Exception {
		
		MetadataRepositoryCrawler crawler = harvesters.get(metadataRepositoryType);
		Assert.notNull(crawler, "Unsupported MetadataRepositoryType:"+metadataRepositoryType);
		crawler.crawl(uri, recursive);
		
	}

}
