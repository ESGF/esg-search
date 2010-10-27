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

import org.springframework.context.support.ClassPathXmlApplicationContext;

import esg.search.harvest.api.HarvestingService;
import esg.search.harvest.api.MetadataRepositoryType;

/**
 * Main class to start the indexing or scrabbing of search records from a remote metadata repository.
 * @author luca.cinquini
 *
 */
public class HarvestingServiceMain {
	
	
    private static String[] configLocations = new String[] { "classpath:esg/search/config/harvest-context.xml" };
    
	public static void main(String[] args) throws Exception {
		
	    final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(configLocations);
	    
	    if (args.length!=3) {
	    	System.out.println("Usage: java esg.search.harvest.impl.HarvestingServiceMain <Metadata Repository URL> <Metadata repository Type> true|false");
	    	System.out.println("       true:insert, false:delete");
	    	System.out.println("Example: java esg.search.harvest.impl.HarvestingServiceMain file:///Users/cinquini/Documents/workspace/esg-search/resources/pcmdi.ipcc4.GFDL.gfdl_cm2_0.picntrl.mon.land.run1.v1.xml THREDDS true|false");
	    	System.out.println("Example: java esg.search.harvest.impl.HarvestingServiceMain http://pcmdi3.llnl.gov/thredds/esgcet/catalog.xml THREDDS true|false");
	    	System.out.println("Example: java esg.search.harvest.impl.HarvestingServiceMain http://esg-datanode.jpl.nasa.gov/thredds/esgcet/catalog.xml THREDDS true|false");
	    	System.out.println("Example: java esg.search.harvest.impl.HarvestingServiceMain file:///Users/cinquini/Documents/workspace/esg-search/resources/ORNL-oai_dif.xml OAI true|false");
	    	System.out.println("Example: java esg.search.harvest.impl.HarvestingServiceMain file:///Users/cinquini/Documents/workspace/esg-search/resources/cas_rdf.xml CAS true|false");
	    	System.exit(-1);
	    }

	    final String uri = args[0];
	    final MetadataRepositoryType type = MetadataRepositoryType.valueOf(args[1]);
	    final boolean add = Boolean.parseBoolean(args[2]);
	    
	    if (add) {
		    final HarvestingService harvestingService = (HarvestingService)context.getBean("indexingService");
	    	harvestingService.harvest(uri, true, type);
	    } else {
		    final HarvestingService harvestingService = (HarvestingService)context.getBean("scrabbingService");
	    	harvestingService.harvest(uri, true, type);
	    }
		
	}
	
}
