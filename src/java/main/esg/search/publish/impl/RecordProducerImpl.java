/*******************************************************************************
 * Copyright (c) 2010 Earth System Grid Federation
 * ALL RIGHTS RESERVED. 
 * U.S. Government sponsorship acknowledged.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of the <ORGANIZATION> nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package esg.search.publish.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import esg.search.core.Record;
import esg.search.publish.api.RecordConsumer;
import esg.search.publish.api.RecordProducer;

/**
 * Straightforward implementation of {@link RecordProducer} 
 * that immediately notifies all subscribed consumers whenever a new search record is produced
 * (i.e. there is no queuing or multi-threaded functionality).
 */
public class RecordProducerImpl implements RecordProducer {
	
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
     * Method for bulk synchronous notification of generated records to all subscribed consumers.
     * @param record
     * @throws Exception
     */
    public void notify(final Collection<Record> records) throws Exception {
        for (final RecordConsumer consumer : consumers) {
            consumer.consume(records);
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
