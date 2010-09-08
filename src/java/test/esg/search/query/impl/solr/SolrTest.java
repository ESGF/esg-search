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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.util.AbstractSolrTestCase;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.springframework.core.io.ClassPathResource;

import esg.search.core.Record;
import esg.search.core.RecordSerializer;
import esg.search.core.RecordSerializerSolrImpl;
import esg.search.utils.XmlParser;

/**
 * Test class for {@link SearchServiceImpl}.
 * This class uses the test harness provided by the Solr libraries to create/use/destroy a temporary solr index.
 *
 */
public class SolrTest extends AbstractSolrTestCase {
	
	// define the location of the Solr configuration files
	static {
		System.setProperty("solr.solr.home", System.getProperty("user.dir")+"/src/java/test/solr");
	}
	
	// reference to solr schema
	 public String getSchemaFile() { return System.getProperty("solr.solr.home")+"/conf/schema.xml"; }
	 
	 // reference to solr configuration
	 public String getSolrConfigFile() { return System.getProperty("solr.solr.home")+"/conf/solrconfig.xml"; }
	 
	 private static final ClassPathResource DATADIR = new ClassPathResource("solr/data/");
	 
	 private final Log LOG = LogFactory.getLog(this.getClass());
	 
	 /**
	  * Tests ingestion of XML records from DATADIR directory into temporary Solr index,
	  * and basic queries.
	  */
	 public void test()  throws IOException, JDOMException  {
		 
		 	// test document ingestion
		 	final File dataDir = DATADIR.getFile();
		 	final XmlParser xmlParser = new XmlParser(false);
		 	final RecordSerializer serializer = new RecordSerializerSolrImpl();
		 	for (final File file : dataDir.listFiles( (FileFilter)FileFilterUtils.suffixFileFilter("xml")) ) {
		 		
		 		if (LOG.isInfoEnabled()) LOG.info(file.getAbsolutePath());
		 		
		 		final Document doc = xmlParser.parseFile( file.getAbsolutePath() );
		 		final Record record = serializer.deserialize(doc.getRootElement());
		 		final List<String> fields = toList(record);
		 		assertU( adoc(fields.toArray(new String[fields.size()])) );
		 		
		 	}
		 	
		 	// commit and optimize index
		    assertU(commit());
		    assertU(optimize());
		 		 
		    // test search
		    assertQ("couldn't find humidity",
		            req("humidity")
		            ,"//result[@numFound=1]"
		            ,"//str[@name='id'][.='id2']"
		            );	 	
		 
	 }
	 
	 /**
	  * Method that returns a flattened search record as a list of (name, value) pairs.
	  * @param record
	  * @return
	  */
	 private List<String> toList(final Record record) {
		 
		 final List<String> fields = new ArrayList<String>();
		 fields.add(SolrXmlPars.FIELD_ID);
		 fields.add(record.getId());
		 final Map<String, List<String>> map = record.getFields();
		 for (final String key : map.keySet()) {
			 for (final String value : map.get(key)) {
				 fields.add(key);
				 fields.add(value);
			 }
		 }
		 return fields;
	 }

}
