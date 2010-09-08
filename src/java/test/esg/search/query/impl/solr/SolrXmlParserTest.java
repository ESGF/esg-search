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
package esg.search.query.impl.solr;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.JDOMException;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import esg.search.core.Record;
import esg.search.query.api.Facet;
import esg.search.query.api.SearchInput;
import esg.search.query.api.SearchOutput;

/**
 * Test class for {@link SolrXmlParser}.
 */
public class SolrXmlParserTest {
	
	private final static ClassPathResource XMLFILE = new ClassPathResource("esg/search/query/impl/solr/response.xml");
	
	private SolrXmlParser solrXmlParser = new SolrXmlParser();
	
	private final Log LOG = LogFactory.getLog(this.getClass());
	
	
	/**
	 * Tests parsing of the <result>....</result> XML snippet containing the search results.
	 * @throws IOException
	 * @throws JDOMException
	 */
	@Test
	public void parseResults() throws IOException, JDOMException {
		
		// read file
		final String xml = FileUtils.readFileToString( XMLFILE.getFile() );
		if (LOG.isDebugEnabled()) LOG.debug(xml);
		
		// parse XML into objects
		final SearchOutput output = solrXmlParser.parseResults(xml);
		if (LOG.isDebugEnabled()) LOG.debug(output.toString());
		
		// <result name="response" numFound="186" start="11">
		Assert.assertEquals(186, output.getCounts());
		Assert.assertEquals(11, output.getOffset());
		
		int iRecord = 0;
		for (final Record record : output.getResults()) {
			
			// <str name="id">id #0</str>
			Assert.assertEquals("id #"+iRecord, record.getId());
			final Map<String, List<String>> fields = record.getFields();
			// <str name="title">title #0</str>
			Assert.assertEquals(fields.get("title").get(0), "title #"+iRecord);
			// <str name="type">Dataset</str>
			Assert.assertEquals(fields.get("type").get(0), "Dataset");
			// <str name="url">http://localhost/access?id=0</str>
			Assert.assertEquals(fields.get("url").get(0), "http://localhost/access?id="+iRecord);
			// <arr name="description"><str>description #0</str></arr>
			Assert.assertEquals(fields.get("description").get(0), "description #"+iRecord);
			// <arr name="variable"><str>variable A</str><str>variable B</str></arr>
			Assert.assertEquals(fields.get("variable").get(0), "variable A");
			Assert.assertEquals(fields.get("variable").get(1), "variable B");
			// <arr name="instrument"><str>instrument #2</str></arr>
			Assert.assertEquals(fields.get("instrument").get(0), "instrument #"+iRecord);
			// <arr name="project"><str>project A</str><str>project B</str></arr>
			Assert.assertEquals(fields.get("project").get(0), "project A");
			Assert.assertEquals(fields.get("project").get(1), "project B");
			// <date name="timestamp">2000-01-01T00:00:00.000Z</date>
			Assert.assertEquals(fields.get("timestamp").get(0), "2000-01-01T00:00:00.000Z");
			
			iRecord++;
		}
		
	}
	
	/**
	 * Tests parsing of the <lst name="facet_counts">...</lst> XML snippet containing the facet counts for the retrieved results.
	 */
	@Test
	public void parseFacets() throws IOException, JDOMException {
		
		// read file
		final String xml = FileUtils.readFileToString( XMLFILE.getFile() );
		if (LOG.isDebugEnabled()) LOG.debug(xml);
		
		// simulate facet constraint in query
		final SearchInput input = new SearchInputImpl();
		input.addConstraint("project", "project A");
		
		// parse XML into objects
		final Map<String, Facet> facets = solrXmlParser.parseFacets(xml, input);
		if (LOG.isDebugEnabled()) {
			for (final Facet facet : facets.values()) {
				LOG.debug(facet.toString());
			}
		}
		
		/**
		 *  Note: unconstrained facet with two non-zero options -> 2 sub-facets
			<lst name="instrument">
				<int name="instrument #0">1</int>
				<int name="instrument #1">1</int>
			</lst>
	    */
		Assert.assertTrue(facets.get("instrument").getKey().equals("instrument"));
		Assert.assertTrue(facets.get("instrument").getLabel().equals("instrument"));
		Assert.assertEquals(2,facets.get("instrument").getSubFacets().size());
		Assert.assertEquals("instrument #0",facets.get("instrument").getSubFacets().get(0).getKey());
		Assert.assertEquals("instrument #0",facets.get("instrument").getSubFacets().get(0).getLabel());
		Assert.assertEquals(1,facets.get("instrument").getSubFacets().get(0).getCounts());
		Assert.assertEquals("instrument #1",facets.get("instrument").getSubFacets().get(1).getLabel());
		Assert.assertEquals("instrument #1",facets.get("instrument").getSubFacets().get(1).getKey());
		Assert.assertEquals(1,facets.get("instrument").getSubFacets().get(1).getCounts());		
		
		/**
		 *  Note: constrained facet -> 1 sub-facet only
		 * 	<lst name="project">
		 *		<int name="project A">1</int>
		 *		<int name="project B">0</int>
		 *	</lst>
		 */
		Assert.assertTrue(facets.get("project").getKey().equals("project"));
		Assert.assertTrue(facets.get("project").getLabel().equals("project"));
		Assert.assertEquals(1,facets.get("project").getSubFacets().size());
		Assert.assertEquals("project A",facets.get("project").getSubFacets().get(0).getKey());
		Assert.assertEquals("project A",facets.get("project").getSubFacets().get(0).getLabel());
		Assert.assertEquals(1,facets.get("project").getSubFacets().get(0).getCounts());
		
		/**
		 * Note: unconstrained facet with only one non-zero option --> 1 sub-facet only
			<lst name="variable">
				<int name="variable A">1</int>
				<int name="variable B">0</int>
			</lst>
		 */
		Assert.assertTrue(facets.get("variable").getKey().equals("variable"));
		Assert.assertTrue(facets.get("variable").getLabel().equals("variable"));
		Assert.assertEquals(1,facets.get("variable").getSubFacets().size());
		Assert.assertEquals("variable A",facets.get("variable").getSubFacets().get(0).getKey());
		Assert.assertEquals("variable A",facets.get("variable").getSubFacets().get(0).getLabel());

		
	}

}
