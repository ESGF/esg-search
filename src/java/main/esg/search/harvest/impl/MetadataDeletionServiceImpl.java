package esg.search.harvest.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import esg.search.core.Record;
import esg.search.core.RecordImpl;
import esg.search.harvest.api.MetadataDeletionService;
import esg.search.harvest.api.RecordConsumer;

/**
 * Implementation of {@link MetadataDeletionService} that produces stub records 
 * and sends them to a collaborating {@link RecordConsumer} for removal from the system.
 * 
 * @author luca.cinquini
 *
 */
@Component
public class MetadataDeletionServiceImpl extends RecordProducerImpl implements MetadataDeletionService {

	@Override
	public void delete(List<String> ids) throws Exception {
		
		for (final String id : ids) {		
			final Record record = new RecordImpl(id);
			this.notify(record);
		}

	}

}
