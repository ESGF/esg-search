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
package esg.search.harvest.impl;

import java.net.URI;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import esg.search.harvest.api.HarvestingService;
import esg.search.harvest.api.MetadataRepositoryType;

public class HarvestingServiceMain {
	
	
    private static String[] configLocations = new String[] { "classpath:esg/search/config/harvest-context.xml" };
	
	//private static final Log LOG = LogFactory.getLog(ThreddsParserMain.class);
    
	//private final static String URL = "http://pcmdi3.llnl.gov/thredds/esgcet/14/pcmdi.ipcc4.UKMO.ukmo_hadgem1.amip.mon.land.run1.v1.xml#pcmdi.ipcc4.UKMO.ukmo_hadgem1.amip.mon.land.run1.v1";
    //private final static String URL = "http://pcmdi3.llnl.gov/thredds/esgcet/catalog.xml";
    //private final static String URL = "file:///Users/cinquini/Documents/workspace/solr/XML/ORNL-oai_dif.xml";
    private final static String URL  = "file:///Users/cinquini/Documents/workspace/solr/XML/cas_rdf.xml";

	
	public static void main(String[] args) throws Exception {
		
	    final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(configLocations);
	    final HarvestingService harvestingService = (HarvestingService)context.getBean("harvestingService");

	    //harvestingService.harvest(new URI(URL), true, MetadataRepositoryType.THREDDS);
	    //harvestingService.harvest(new URI(URL), true, MetadataRepositoryType.OAI);
	    harvestingService.harvest(new URI(URL), true, MetadataRepositoryType.CAS);
		
	}
	
}
