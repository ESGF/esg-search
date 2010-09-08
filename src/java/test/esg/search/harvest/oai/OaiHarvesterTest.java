package esg.search.harvest.oai;

import java.net.URI;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import esg.search.core.Record;
import esg.search.harvest.impl.InMemoryStore;
import esg.search.harvest.xml.dif.MetadataHandlerDifImpl;

/**
 * Test class for {@link OaiHarvester}.
 *
 */
public class OaiHarvesterTest {
	
	private final static ClassPathResource XMLFILE = new ClassPathResource("esg/search/harvest/oai/oai_dif.xml");
	
	OaiHarvester oaiHarvester;
	InMemoryStore consumer;
	
	@Before
	public void setup() {
		oaiHarvester = new OaiHarvester( new MetadataHandlerDifImpl() );
		consumer = new InMemoryStore();
		oaiHarvester.subscribe(consumer);
		
	}
	
	/**
	 * Tests crawling of a OAI/DIF XML document (as serialized to the file system).
	 * @throws Exception
	 */
	@Test
	public void crawl() throws Exception {
		
		final URI uri = new URI( "file://"+XMLFILE.getFile().getAbsolutePath() );
		oaiHarvester.crawl(uri, true);
		
		// tests number of metadata records
		// note: "deleted" records are ignored
		final List<Record> records = consumer.getRecords();
		Assert.assertTrue(records.size()==2);
		
		
	}

}
