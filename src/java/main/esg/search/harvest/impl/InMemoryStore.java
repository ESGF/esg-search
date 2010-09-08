package esg.search.harvest.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import esg.search.core.Record;
import esg.search.harvest.api.RecordConsumer;

/**
 * Implementation of {@link RecordConsumer} that stores in-memory all the records it receives.
 */
public class InMemoryStore implements RecordConsumer {
	
	private List<Record> records = new ArrayList<Record>();

	/**
	 * Method implementation that adds the given record to the in-memory list.
	 */
	public void consume(Record record) throws Exception {
		records.add(record);
	}
	
	/**
	 * Method to retrieve all the records consumed so far.
	 * @return
	 */
	public List<Record> getRecords() {
		return Collections.unmodifiableList(records);
	}

}
