package esg.search.harvest.thredds;

import java.net.URI;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import esg.search.core.Record;
import esg.search.harvest.impl.InMemoryStore;

/**
 * Test class for {@link ThreddsHarvester}.
 *
 */
public class ThreddsHarvesterTest {
	
	private final static ClassPathResource XMLFILE = new ClassPathResource("esg/search/harvest/thredds/root_catalog.xml");
	
	ThreddsHarvester threddsHarvester;
	InMemoryStore consumer;
		
	@Before
	public void setup() {
		threddsHarvester = new ThreddsHarvester( new ThreddsParserStrategyTopLevelDatasetImpl() );
		consumer = new InMemoryStore();
		threddsHarvester.subscribe(consumer);
		
	}
	
	/**
	 * Tests crawling of a THREDDS root catalog
	 * (i.e. the recursive behavior of harvesting a THREDDS catalogs hierarchy).
	 * @throws Exception
	 */
	@Test
	public void crawl() throws Exception {
		
		final URI uri = new URI( "file://"+XMLFILE.getFile().getAbsolutePath() );
		threddsHarvester.crawl(uri, true);
		
		// tests number of metadata records
		final List<Record> records = consumer.getRecords();
		Assert.assertEquals(2,records.size());
		
	}

}
