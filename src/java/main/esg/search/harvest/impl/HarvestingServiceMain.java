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
    
	public static void main(String[] args) throws Exception {
		
	    final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(configLocations);
	    final HarvestingService harvestingService = (HarvestingService)context.getBean("harvestingService");
	    
	    if (args.length!=2) {
	    	System.out.println("Usage: java esg.search.harvest.impl.HarvestingServiceMain <Metadata Repository URL> <Metadata repository Type>");
	    	System.out.println("Example: java esg.search.harvest.impl.HarvestingServiceMain file:///Users/cinquini/Documents/workspace/esg-search/resources/pcmdi.ipcc4.GFDL.gfdl_cm2_0.picntrl.mon.land.run1.v1.xml THREDDS");
	    	System.out.println("Example: java esg.search.harvest.impl.HarvestingServiceMain http://pcmdi3.llnl.gov/thredds/esgcet/catalog.xml THREDDS");
	    	System.out.println("Example: java esg.search.harvest.impl.HarvestingServiceMain http://esg-datanode.jpl.nasa.gov/thredds/esgcet/catalog.xml THREDDS");
	    	System.out.println("Example: java esg.search.harvest.impl.HarvestingServiceMain file:///Users/cinquini/Documents/workspace/esg-search/resources/ORNL-oai_dif.xml OAI");
	    	System.out.println("Example: java esg.search.harvest.impl.HarvestingServiceMain file:///Users/cinquini/Documents/workspace/esg-search/resources/cas_rdf.xml CAS");
	    	System.exit(-1);
	    }

	    final URI uri = new URI(args[0]);
	    final MetadataRepositoryType type = MetadataRepositoryType.valueOf(args[1]);
	    
	    harvestingService.harvest(uri, true, type);
		
	}
	
}
