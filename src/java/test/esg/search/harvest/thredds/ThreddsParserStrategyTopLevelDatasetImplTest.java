package esg.search.harvest.thredds;

import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import thredds.catalog.InvCatalog;
import thredds.catalog.InvCatalogFactory;
import thredds.catalog.InvDataset;
import esg.search.core.Record;

/**
 * Test class for {@link ThreddsParserStrategyTopLevelDatasetImpl}.
 * @author luca.cinquini
 *
 */
public class ThreddsParserStrategyTopLevelDatasetImplTest {
	
	private final static ClassPathResource XMLFILE = new ClassPathResource("esg/search/harvest/thredds/catalog.xml");
	
	private final Log LOG = LogFactory.getLog(this.getClass());
	
	/**
	 * Tests the parsing of a {@link InvDataset} object into a single {@link Record} object.
	 */
	@Test
	public void testParseDataset() throws Exception {
		
		final InvCatalogFactory factory = new InvCatalogFactory("default", true); // validate=true
		final InvCatalog catalog = factory.readXML( XMLFILE.getURI() );
		final InvDataset dataset = catalog.getDatasets().get(0);
		ThreddsParserStrategy parser = new ThreddsParserStrategyTopLevelDatasetImpl();
		
		// tests number of metadata records
		final List<Record> records = parser.parseDataset(dataset);
		Assert.assertTrue(records.size()==1);
		
		// test record fields
		final Record record = records.get(0);
		if (LOG.isInfoEnabled()) LOG.info(record);
		Assert.assertTrue(record.getId().equals("pcmdi.ipcc4.UKMO.ukmo_hadgem1.amip.mon.land.run1.v1"));
		
		final Map<String, List<String>> fields = record.getFields();
		Assert.assertTrue(fields.get("type").contains("Dataset"));
		Assert.assertTrue(fields.get("title").contains("IPCC Fourth Assessment Report"));
		Assert.assertTrue(fields.get("description").contains("Met Office  model output prepared for IPCC Fourth Assessment AMIP experiment"));
		Assert.assertTrue(fields.get("url").get(0).matches("file:(.+)esg/search/harvest/thredds/catalog.xml#pcmdi.ipcc4.UKMO.ukmo_hadgem1.amip.mon.land.run1.v1$"));
		Assert.assertTrue(fields.get("experiment").contains("amip"));
		Assert.assertTrue(fields.get("format").contains("netCDF, CF-1.0"));
		Assert.assertTrue(fields.get("institute").contains("UKMO"));
		Assert.assertTrue(fields.get("model").contains("ukmo_hadgem1"));
		Assert.assertTrue(fields.get("project").contains("ipcc4"));
		Assert.assertTrue(fields.get("realm").contains("land"));
		Assert.assertTrue(fields.get("run_name").contains("run1"));
		Assert.assertTrue(fields.get("time_frequency").contains("mon"));
		Assert.assertTrue(fields.get("cf_variable").contains("Soil Frozen Water Content"));
		Assert.assertTrue(fields.get("cf_variable").contains("Snow Amount"));
		Assert.assertTrue(fields.get("cf_variable").contains("Surface and Subsurface Runoff"));
		Assert.assertTrue(fields.get("cf_variable").contains("Surface Runoff"));
		Assert.assertTrue(fields.get("cf_variable").contains("Total Soil Moisture Content"));
		Assert.assertTrue(fields.get("creation_time").contains("2010-03-31 15:52:44"));
		Assert.assertTrue(fields.get("dataset_id").contains("pcmdi.ipcc4.UKMO.ukmo_hadgem1.amip.mon.land.run1"));
		Assert.assertTrue(fields.get("dataset_version").contains("1"));
		Assert.assertTrue(fields.get("variable").contains("mrfso"));
		Assert.assertTrue(fields.get("variable").contains("snw"));
		Assert.assertTrue(fields.get("variable").contains("mrro"));
		Assert.assertTrue(fields.get("variable").contains("mrros"));
		Assert.assertTrue(fields.get("variable").contains("mrso"));	
		
	}

}
