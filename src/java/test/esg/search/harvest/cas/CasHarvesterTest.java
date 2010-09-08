package esg.search.harvest.cas;

import java.net.URI;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import esg.search.core.Record;
import esg.search.harvest.impl.InMemoryStore;
import esg.search.harvest.xml.cas.MetadataHandlerCasRdfImpl;

/**
 * Test class for {@link CasHarvester}.
 *
 */
public class CasHarvesterTest {
	
	private final static ClassPathResource XMLFILE = new ClassPathResource("esg/search/harvest/xml/cas/cas_rdf.xml");
	
	CasHarvester casHarvester;
	InMemoryStore consumer;
	
	@Before
	public void setup() {
		casHarvester = new CasHarvester( new MetadataHandlerCasRdfImpl() );
		consumer = new InMemoryStore();
		casHarvester.subscribe(consumer);
		
	}
	
	/**
	 * Tests crawling of a CAS XML document (as serialized to the file system).
	 * @throws Exception
	 */
	@Test
	public void crawl() throws Exception {
		
		final URI uri = new URI( "file://"+XMLFILE.getFile().getAbsolutePath() );
		casHarvester.crawl(uri, true);
		
		final List<Record> records = consumer.getRecords();
		Assert.assertTrue(records.size()==2);
		
		
	}

}
