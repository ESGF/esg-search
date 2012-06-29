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
package esg.search.publish.thredds;

import java.io.File;
import java.net.URI;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import esg.search.core.Record;
import esg.search.publish.impl.InMemoryStore;
import esg.search.publish.impl.RecordProducerImpl;

/**
 * Test class for {@link ThreddsCrawler}.
 *
 */
public class ThreddsCrawlerTest {
	
	private final static ClassPathResource XMLFILE = new ClassPathResource("esg/search/publish/thredds/root_catalog.xml");
	
	ThreddsCrawler threddsHarvester;
	InMemoryStore consumer;
	RecordProducerImpl producer;
		
	@Before
	public void setup() {
		threddsHarvester = new ThreddsCrawler( new ThreddsParserStrategyTopLevelDatasetImpl(), null ); // no search service
		consumer = new InMemoryStore();
		producer = new RecordProducerImpl();
		producer.subscribe(consumer);
		
	}
	
	/**
	 * Tests crawling of a THREDDS root catalog
	 * (i.e. the recursive behavior of harvesting a THREDDS catalogs hierarchy).
	 * @throws Exception
	 */
	@Test
	public void crawl() throws Exception {
		//made more OS friendly
	    String localPath = XMLFILE.getFile().getAbsolutePath();
	    if (localPath.charAt(0) != '/') {
	        //windows!  I know... but perhaps other OSs too :-)
	        localPath = "/" + localPath.replace(File.separatorChar, '/');
	    }
		final URI uri = new URI( "file://" + localPath );
		threddsHarvester.crawl(uri, null, true, producer, true);
		
		// tests number of metadata records
		final Map<String, Record> records = consumer.getRecords();
		Assert.assertEquals(18,records.size());		
		Assert.assertEquals(1,records.get("pcmdi.ipcc4.UKMO.ukmo_hadgem1.amip.mon.land.run1.v1|localhost").getVersion());
		Assert.assertEquals(2,records.get("pcmdi.ipcc4.UKMO.ukmo_hadgem1.amip.mon.land.run1.v2|localhost").getVersion());
		Assert.assertEquals(1,records.get("pcmdi.ipcc4.UKMO.ukmo_hadgem1.amip.mon.land.run2.v1|localhost").getVersion());
	}

}
