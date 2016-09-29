package esg.search.publish.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import esg.search.core.Record;
import esg.search.core.RecordImpl;
import esg.search.publish.api.MetadataRetractionService;
import esg.search.publish.api.RecordConsumer;

/**
 * Implementation of {@link MetadataRetractionService} that produces stub records 
 * and sends them to a collaborating {@link RecordConsumer} for removal from the system.
 * 
 * @author luca.cinquini
 *
 */
@Component("recordRetractor")
public class MetadataRetractionServiceImpl extends RecordProducerImpl implements MetadataRetractionService {
	
	@Autowired
	public MetadataRetractionServiceImpl(final @Qualifier("retractor") RecordConsumer retractor) {
		this.subscribe(retractor);
	}

	@Override
	public void retract(List<String> ids) throws Exception {
		
		for (final String id : ids) {		
			final Record record = new RecordImpl(id);
			this.notify(record);
		}

	}

}
