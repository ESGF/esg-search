package esg.search.publish.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import esg.search.publish.api.MetadataRetractionService;
import esg.search.publish.api.RecordConsumer;

/**
 * Implementation of {@link MetadataRetractionService} that retracts records in the local Solr index.
 * 
 * @author luca.cinquini
 *
 */
@Component("recordRetractorLocal")
public class MetadataRetractionServiceImplLocal extends MetadataRetractionServiceImpl {
	
	@Autowired
	public MetadataRetractionServiceImplLocal(final @Qualifier("retractorLocal") RecordConsumer retractor) {
		super(retractor);
	}

}
