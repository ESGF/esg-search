package esg.search.harvest.impl;

import java.util.List;

import esg.search.harvest.api.MetadataDeletionService;
import esg.search.harvest.api.MetadataRepositoryCrawlerManager;
import esg.search.harvest.api.MetadataRepositoryType;
import esg.search.harvest.api.PublishingService;

/**
 * Implementation of {@link PublishingService} that delegates all functionality
 * to collaborating beans for crawling remote metadata repositories, producing
 * search records, and consuming search records for ingestion or removal.
 * 
 * @author luca.cinquini
 *
 */
public class PublishingServiceImpl implements PublishingService {
	
	/**
	 * Collaborator that crawls remote metadata repositories for the purpose of publishing records into the system.
	 */
	private final MetadataRepositoryCrawlerManager publishingCrawler;
	
	/**
	 * Collaborator that crawls remote metadata repositories for the purpose of unpublishing records from the system.
	 */
	private final MetadataRepositoryCrawlerManager unpublishingCrawler;
	
	/**
	 * Collaborator that deletes records with known identifiers.
	 */
	private final MetadataDeletionService recordRemover;

	public PublishingServiceImpl(final MetadataRepositoryCrawlerManager publishingCrawler,
			                     final MetadataRepositoryCrawlerManager unpublishingCrawler,
			                     final MetadataDeletionService recordRemover) {
		
		this.publishingCrawler = publishingCrawler;
		this.unpublishingCrawler = unpublishingCrawler;
		this.recordRemover = recordRemover;
	}

	@Override
	public void publish(String uri, boolean recursive, MetadataRepositoryType metadataRepositoryType) throws Exception {
		
		publishingCrawler.crawl(uri, recursive, metadataRepositoryType);

	}

	@Override
	public void unpublish(String uri, boolean recursive,MetadataRepositoryType metadataRepositoryType) throws Exception {
		
		unpublishingCrawler.crawl(uri, recursive, metadataRepositoryType);

	}

	@Override
	public void unpublish(List<String> ids) throws Exception {
		
		recordRemover.delete(ids);

	}

}
