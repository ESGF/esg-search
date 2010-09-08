package esg.search.harvest.api;

import esg.search.core.Record;

public interface RecordConsumer {
	
	void consume(Record record) throws Exception;

}
