package esg.search.publish.impl;

import java.util.Collection;

import esg.search.core.Record;
import esg.search.publish.api.RecordConsumer;

/**
 * Convenient superclass of record consumers that can only process one record at a time.
 * 
 * @author Luca Cinquini
 *
 */
public abstract class SingleRecordConsumer implements RecordConsumer {

    @Override
    public void consume(Collection<Record> records) throws Exception {
        
        for (final Record record : records) {
            this.consume(record);
        }

    }

}
