package esg.search.harvest.thredds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import thredds.catalog.InvCatalog;
import thredds.catalog.InvCatalogFactory;
import thredds.catalog.InvDataset;

/**
 * Test class for {@link ThreddsDatasetUrlBuilderCatalogUrlImpl}.
 * @author luca.cinquini
 *
 */
public class ThreddsDatasetUrlBuilderCatalogUrlImplTest {
	
	private final static ClassPathResource XMLFILE = new ClassPathResource("esg/search/harvest/thredds/catalog.xml");
	
	private final Log LOG = LogFactory.getLog(this.getClass());
	
	/**
	 * Tests the construction of the URL associated with a THREDDS metadata record.
	 * @throws Exception
	 */
	@Test
	public void testBuildUrl() throws Exception {
		
		final InvCatalogFactory factory = new InvCatalogFactory("default", true); // validate=true
		final InvCatalog catalog = factory.readXML( XMLFILE.getURI() );
		final InvDataset dataset = catalog.getDatasets().get(0);
		
		final ThreddsDatasetUrlBuilderCatalogUrlImpl target = new ThreddsDatasetUrlBuilderCatalogUrlImpl();
		final String url = target.buildUrl(dataset);
		if (LOG.isDebugEnabled()) LOG.info("Dataset URL="+url);
		Assert.assertTrue(url.matches("file:(.+)esg/search/harvest/thredds/catalog.xml#pcmdi.ipcc4.UKMO.ukmo_hadgem1.amip.mon.land.run1.v1$"));
		
	}

}
