package esg.search.publish.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import esg.search.publish.api.MetadataDeletionService;
import esg.search.publish.api.RecordConsumer;

/**
 * Implementation of {@link MetadataDeletionService} that deletes records from the local Solr index.
 * 
 * @author luca.cinquini
 *
 */
@Component("recordRemoverLocal")
public class MetadataDeletionServiceImplLocal extends MetadataDeletionServiceImpl {
	
	@Autowired
	public MetadataDeletionServiceImplLocal(final @Qualifier("scrabberLocal") RecordConsumer scrabber) {
		super(scrabber);
	}

}
