package esg.search.harvest.impl;

import java.util.ArrayList;
import java.util.List;

import esg.search.core.Record;
import esg.search.harvest.api.RecordConsumer;
import esg.search.harvest.api.RecordProducer;

/**
 * Straightforward implementation of {@link RecordProducer} 
 * that immediately notifies all subscribed consumers whenever a new search record is produced
 * (i.e. there is no queuing or multi-threaded functionality).
 */
public abstract class RecordProducerImpl implements RecordProducer {
	
	private List<RecordConsumer> consumers = new ArrayList<RecordConsumer>();

	/**
	 * {@inheritDoc}
	 */
	public void subscribe(final RecordConsumer consumer) {
		consumers.add(consumer);
	}

	/**
	 * {@inheritDoc}
	 */
	public void unsubscribe(final RecordConsumer consumer) {
		consumers.remove(consumer);
	}
	
	/**
	 * Method for synchronous notification of generated records to all subscribed consumers.
	 * @param record
	 * @throws Exception
	 */
	public void notify(final Record record) throws Exception {
		for (final RecordConsumer consumer : consumers) {
			consumer.consume(record);
		}
	}
	
	/**
	 * Method to bulk-subscribe a list of record consumers
	 * (and automatically un-subscribe all previously consumers).
	 * @param consumers
	 */
	public void setConsumers(final List<RecordConsumer> consumers) {
		this.consumers.clear();
		this.consumers.addAll(consumers);
	}

}
