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
