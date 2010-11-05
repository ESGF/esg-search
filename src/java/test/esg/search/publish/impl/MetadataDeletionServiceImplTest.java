package esg.search.publish.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import esg.search.core.Record;
import esg.search.publish.impl.InMemoryStore;
import esg.search.publish.impl.MetadataDeletionServiceImpl;

public class MetadataDeletionServiceImplTest {
	
	private MetadataDeletionServiceImpl service;
	private InMemoryStore consumer;
	
	@Before
	public void setup() {
		
		consumer = new InMemoryStore();
		service = new MetadataDeletionServiceImpl(consumer);
		
	}
	
	@Test
	public void testDelete() throws Exception {
		
		final List<String> ids = Arrays.asList( new String[] { "aaa", "bbb" } );
		service.delete(ids);
		
		// map of records keyed by identifier
		final Map<String, Record> records = consumer.getRecords();
		Assert.assertEquals(2,records.size());
		for (final String id : ids) {
			Assert.assertTrue(records.containsKey(id));
		}
		
	}

}
