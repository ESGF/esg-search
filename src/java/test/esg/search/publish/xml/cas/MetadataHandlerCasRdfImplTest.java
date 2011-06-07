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
package esg.search.publish.xml.cas;

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
import esg.search.publish.xml.MetadataHandler;
import esg.search.publish.xml.cas.MetadataHandlerCasRdfImpl;
import esg.search.utils.XmlParser;

/**
 * Test class for {@link MetadataHandlerCasRdfImpl}.
 */
public class MetadataHandlerCasRdfImplTest {
	
	private final static ClassPathResource XMLFILE = new ClassPathResource("esg/search/publish/xml/cas/cas_rdf.xml");
	
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

		Assert.assertTrue(fields.get("datetime_start").contains("2008-04-16T00:00:00.000000Z"));
		Assert.assertTrue(fields.get("datetime_stop").contains("2008-04-16T23:59:59.999999Z"));
		
		Assert.assertTrue(fields.get("east_degrees").contains("180.0"));
		Assert.assertTrue(fields.get("north_degrees").contains("90.0"));
		Assert.assertTrue(fields.get("south_degrees").contains("-90.0"));
		Assert.assertTrue(fields.get("west_degrees").contains("-180.0"));
				
	}

}
