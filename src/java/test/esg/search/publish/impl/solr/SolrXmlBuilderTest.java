package esg.search.publish.impl.solr;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import esg.search.core.Record;
import esg.search.core.RecordImpl;
import esg.search.query.impl.solr.SolrXmlPars;
import esg.search.utils.StringUtils;

public class SolrXmlBuilderTest {
	
	final SolrXmlBuilder solrMessageHandler = new SolrXmlBuilder();
	
	private final Log LOG = LogFactory.getLog(this.getClass());
	
	final Record record = new RecordImpl();
	
	private final static String ID = "test id";
	private final static String TITLE = "test title";
	private final static String DESCRIPTION = "test description";
	private final static String URL = "http://test.com/";
	private final static String PROPERTY_A = "value A";
	private final static String PROPERTY_B = "value B";
	private final static String TYPE = "Dataset";
	private final static String VERSION = "1";

	private final static ClassPathResource DELETE_MESSAGE = new ClassPathResource("esg/search/publish/impl/solr/delete.xml");
	private final static ClassPathResource ADD_MESSAGE = new ClassPathResource("esg/search/publish/impl/solr/add.xml");
	
	@Before
	public void setup() {
		
		record.setId(ID);
		record.addField(SolrXmlPars.FIELD_TITLE, TITLE);
		record.addField(SolrXmlPars.FIELD_TYPE, TYPE);
		record.addField(SolrXmlPars.FIELD_DESCRIPTION, DESCRIPTION);
		record.addField(SolrXmlPars.FIELD_URL, URL);
		record.addField(SolrXmlPars.FIELD_VERSION, VERSION);
		record.addField("property", PROPERTY_A);
		record.addField("property", PROPERTY_B);
		
	}
	
	/**
	 * Tests generation of a <delete> XML document.
	 * @throws IOException
	 */
	@Test
	public void testBuildDeleteMessage() throws IOException {
		
		final List<String> ids = Arrays.asList(new String[]{ "123", "456"} );
		final String xml = solrMessageHandler.buildDeleteMessage(ids, true);
		if (LOG.isInfoEnabled()) LOG.info(xml);
		Assert.assertEquals( StringUtils.compact(FileUtils.readFileToString( DELETE_MESSAGE.getFile() ) ), StringUtils.compact( xml ));
		
	}
	
	/**
	 * Tests generation of an <add> XML document.
	 * @throws IOException
	 */
	@Test
	public void testBuildAddMessage() throws IOException {
		
		String xml = solrMessageHandler.buildAddMessage(record, true);
		if (LOG.isInfoEnabled()) LOG.info(xml);
		Assert.assertEquals( StringUtils.compact(FileUtils.readFileToString( ADD_MESSAGE.getFile() ) ), StringUtils.compact( xml ));
	
	}
	

}
