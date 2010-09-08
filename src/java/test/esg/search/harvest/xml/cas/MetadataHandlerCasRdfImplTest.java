package esg.search.harvest.xml.cas;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import esg.search.core.Record;
import esg.search.harvest.xml.MetadataHandler;
import esg.search.utils.XmlParser;

/**
 * Test class for {@link MetadataHandlerCasRdfImpl}.
 */
public class MetadataHandlerCasRdfImplTest {
	
	private final static ClassPathResource XMLFILE = new ClassPathResource("esg/search/harvest/xml/cas/cas_rdf.xml");
	
	private MetadataHandler metadataHandler = new MetadataHandlerCasRdfImpl();
	
	private final Log LOG = LogFactory.getLog(this.getClass());

	/**
	 * Tests parsing of a CAS/RDF XML document into Record objects.
	 * @throws IOException
	 * @throws JDOMException
	 */
	@Test
	public void parse() throws IOException, JDOMException {
		
		final Document doc = (new XmlParser(false)).parseFile( XMLFILE.getFile().getAbsolutePath() );
		final List<Record> records = metadataHandler.parse(doc.getRootElement());
		
		// check number or records
		Assert.assertTrue(records.size()==2);
		
		// check first record fields
		final Record record = records.get(0);
		if (LOG.isInfoEnabled()) LOG.info(record);
		Assert.assertTrue(record.getId().equals("MLS-Aura_L2GP-CO_v02-23-c01_2008d107.he5"));
		
		final Map<String, List<String>> fields = record.getFields();
		Assert.assertTrue(fields.get("data_format").contains("he5"));
		Assert.assertTrue(fields.get("url").contains("http://jpl-esg.jpl.nasa.gov:8080/esgprod/data?productID=b8be9bab-5c2c-11df-aa0f-251c5cfb68e2"));
		Assert.assertTrue(fields.get("type").contains("Dataset"));
		Assert.assertTrue(fields.get("title").contains("MLS-Aura_L2GP-CO_v02-23-c01_2008d107.he5"));
		Assert.assertTrue(fields.get("project").contains("MLS"));		
		Assert.assertTrue(fields.get("name").contains("MLS-Aura_L2GP-CO_v02-23-c01_2008d107.he5"));

		Assert.assertTrue(fields.get("datetime_start").contains("2008-04-16 00:00:00.000000Z"));
		Assert.assertTrue(fields.get("datetime_stop").contains("2008-04-16 23:59:59.999999Z"));
		
		Assert.assertTrue(fields.get("east_degrees").contains("180.0"));
		Assert.assertTrue(fields.get("north_degrees").contains("90.0"));
		Assert.assertTrue(fields.get("south_degrees").contains("-90.0"));
		Assert.assertTrue(fields.get("west_degrees").contains("-180.0"));
				
	}

}
