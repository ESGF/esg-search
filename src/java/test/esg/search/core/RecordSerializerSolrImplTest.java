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
package esg.search.core;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import esg.search.query.impl.solr.SolrXmlPars;
import esg.search.utils.StringUtils;
import esg.search.utils.XmlParser;


/**
 * Test class for {@link RecordSerializerSolrImpl}.
 */
public class RecordSerializerSolrImplTest {
	
	final RecordSerializer recordSerializer = new RecordSerializerSolrImpl();
	
	final Record record = new RecordImpl();
	
	private final static String ID = "test id";
	private final static String TITLE = "test title";
	private final static String DESCRIPTION = "test description";
	private final static String URL = "http://test.com/";
	private final static String PROPERTY_A = "value A";
	private final static String PROPERTY_B = "value B";
	private final static String TYPE = "Dataset";
	private final static String VERSION = "1";
	
	private final Log LOG = LogFactory.getLog(this.getClass());
	
	private final static ClassPathResource RECORD_IN = new ClassPathResource("esg/search/core/record_in.xml");
	private final static ClassPathResource RECORD_OUT = new ClassPathResource("esg/search/core/record_out.xml");
	
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
	 * Tests serialization of a Record object into XML.
	 * @throws IOException
	 */
	@Test
	public void serialize() throws IOException {
		
		String xml = recordSerializer.serialize(record, true);
		if (LOG.isInfoEnabled()) LOG.info(xml);
		Assert.assertEquals( StringUtils.compact(FileUtils.readFileToString( RECORD_IN.getFile() ) ), StringUtils.compact( xml ));
	
	}
	
	/**
	 * Tests de-serialization of an XML document into a Record object.
	 * @throws IOException
	 * @throws JDOMException
	 */
	@Test
	public void deserialize() throws IOException, JDOMException {
		
		final String xml = FileUtils.readFileToString( RECORD_OUT.getFile() );
		final Document doc = (new XmlParser(false)).parseString(xml);
		final Record record = recordSerializer.deserialize(doc.getRootElement());
		if (LOG.isInfoEnabled()) LOG.info(record);
		Assert.assertTrue(record.getId().equals(ID));
		Assert.assertTrue(new Long(record.getVersion()).toString().equals(VERSION));
		final Map<String, List<String>> fields = record.getFields();
		Assert.assertTrue(fields.get(SolrXmlPars.FIELD_TITLE).contains(TITLE));
		Assert.assertTrue(fields.get(SolrXmlPars.FIELD_DESCRIPTION).contains(DESCRIPTION));
		Assert.assertTrue(fields.get(SolrXmlPars.FIELD_TYPE).contains(TYPE));
		Assert.assertTrue(fields.get("property").contains(PROPERTY_A));
		Assert.assertTrue(fields.get("property").contains(PROPERTY_B));
	
	}
	
	

}
