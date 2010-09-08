package esg.search.harvest.xml.dif;

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
 * Test class for {@link MetadataHandlerDifImpl}.
 */
public class MetadataHandlerDifImplTest {
	
	private final static ClassPathResource XMLFILE = new ClassPathResource("esg/search/harvest/xml/dif/dif.xml");
	
	private MetadataHandler metadataHandler = new MetadataHandlerDifImpl();
	
	private final Log LOG = LogFactory.getLog(this.getClass());

	/**
	 * Tests parsing of a DIF XML document into Record objects.
	 * @throws IOException
	 * @throws JDOMException
	 */
	@Test
	public void parse() throws IOException, JDOMException {
		
		final Document doc = (new XmlParser(false)).parseFile( XMLFILE.getFile().getAbsolutePath() );
		final List<Record> records = metadataHandler.parse(doc.getRootElement());
		
		// check number or records
		Assert.assertTrue(records.size()==1);
		
		// check record fields
		final Record record = records.get(0);
		if (LOG.isInfoEnabled()) LOG.info(record);
		Assert.assertTrue(record.getId().equals("FIFE_STRM_15M"));
		
		final Map<String, List<String>> fields = record.getFields();
		Assert.assertTrue(fields.get("type").contains("Dataset"));
		Assert.assertTrue(fields.get("title").contains("15 MINUTE STREAM FLOW DATA: USGS (FIFE)"));
		Assert.assertTrue(fields.get("description").contains("ABSTRACT: The Fifteen Minute Stream Flow Data from the USGS..."));
		Assert.assertTrue(fields.get("gcmd_variable").contains("HYDROSPHERE > SURFACE WATER > DISCHARGE/FLOW"));
		Assert.assertTrue(fields.get("gcmd_variable").contains("HYDROSPHERE > SURFACE WATER > STAGE HEIGHT"));
		Assert.assertTrue(fields.get("project").contains("ESIP"));
		Assert.assertTrue(fields.get("project").contains("EOSDIS"));
		Assert.assertTrue(fields.get("instrument").contains("STILLING WELL"));
		Assert.assertTrue(fields.get("url").contains("http://daac.ornl.gov/cgi-bin/dsviewer.pl?ds_id=1"));

				
	}

}
