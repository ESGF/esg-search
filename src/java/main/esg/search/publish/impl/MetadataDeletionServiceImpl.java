package esg.search.publish.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import esg.search.core.Record;
import esg.search.core.RecordImpl;
import esg.search.publish.api.MetadataDeletionService;
import esg.search.publish.api.RecordConsumer;

/**
 * Implementation of {@link MetadataDeletionService} that produces stub records 
 * and sends them to a collaborating {@link RecordConsumer} for removal from the system.
 * 
 * @author luca.cinquini
 *
 */
@Component
public class MetadataDeletionServiceImpl extends RecordProducerImpl implements MetadataDeletionService {
	
	@Autowired
	public MetadataDeletionServiceImpl(final @Qualifier("scrabber") RecordConsumer consumer) {
		this.subscribe(consumer);
	}

	@Override
	public void delete(List<String> ids) throws Exception {
		
		for (final String id : ids) {		
			final Record record = new RecordImpl(id);
			this.notify(record);
		}

	}

}
