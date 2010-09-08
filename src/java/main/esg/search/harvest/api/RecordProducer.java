package esg.search.harvest.api;

import esg.search.core.Record;

/**
 * Interface representing a generic producer of search records,
 * which are sent to all subscribed search records consumers.
 */
public interface RecordProducer {
	
	/**
	 * Method used to subscribe a record consumer to this record producer.
	 * @param consumer
	 */
	void subscribe(RecordConsumer consumer);
	
	/**
	 * Method used to un-subscribe a record consumer to this record producer.
	 * @param consumer
	 */
	void unsubscribe(RecordConsumer consumer);
	
	/**
	 * Method to notify all subscribed consumers of a newly produced search record.
	 * @param record
	 */
	void notify(Record record) throws Exception;

}
