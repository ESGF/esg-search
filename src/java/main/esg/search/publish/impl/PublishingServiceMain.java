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
package esg.search.publish.impl;

import java.util.Arrays;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import esg.search.publish.api.MetadataRepositoryType;
import esg.search.publish.api.PublishingService;

/**
 * Main class to start the indexing or scrabbing of search records from a remote metadata repository,
 * or deletion of a single record with known identifier.
 * 
 * @author luca.cinquini
 *
 */
public class PublishingServiceMain {
	
	
    private static String[] configLocations = new String[] { "classpath:esg/search/config/application-context.xml" };
    
    /**
     * Main method loads the proper web service to invoke from the Spring context.
     * 
     * @param args
     * @throws Exception
     */
	public static void main(String[] args) throws Exception {
		
	    final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(configLocations);
	    final PublishingService publishingService = (PublishingService)context.getBean("publishingService");
	    
	    final PublishingServiceMain self = new PublishingServiceMain();
	    self.run(publishingService, args);
	    
	}
	    
	    
	/**
	 * Method to execute the web service invocation.
	 * 
	 * @param publishingService
	 * @param args
	 * @throws Exception
	 */
    protected void run(final PublishingService publishingService, final String[] args) throws Exception {
    	
	    if (args.length!=1 && args.length!=3) {
	    	exit();
	    }
	    
	    
	    // unpublish single record
	    if (args.length==1) {
	    	final String id = args[0];
	    	publishingService.unpublish(Arrays.asList(new String[] {id}));
	    	
	    // publish/unpublish full repository
	    } else if (args.length==3) {
	    	
		    final String uri = args[0];
		    final MetadataRepositoryType type = MetadataRepositoryType.valueOf(args[1]);
		    final boolean publish = Boolean.parseBoolean(args[2]);
		    
		    if (publish) {
		    	publishingService.publish(uri, true, type);
		    } else {
		    	publishingService.unpublish(uri, true, type);
		    }
	    }
		
	}
	
	/**
	 * Method to indicate usage and exit the program.
	 */
	protected void exit() {
		
    	System.out.println("Usage #1: to unpublish a single record:");
    	System.out.println("          java esg.search.publish.impl."+this.getClass().getName()+" <id>");
    	System.out.println("Usage #2: to publish or unpublish a remote metadata repository: ");
    	System.out.println("          java esg.search.publish.impl."+this.getClass().getName()+" <Metadata Repository URL> <Metadata repository Type> true|false");
    	System.out.println("          where true:publish, false:unpublish");
    	System.out.println("Example: java esg.search.publish.impl."+this.getClass().getName()+" nasa.jpl.tes.monthly");
    	System.out.println("Example: java esg.search.publish.impl."+this.getClass().getName()+" file:///Users/cinquini/Documents/workspace/esg-search/resources/pcmdi.ipcc4.GFDL.gfdl_cm2_0.picntrl.mon.land.run1.v1.xml THREDDS true|false");
    	System.out.println("Example: java esg.search.publish.impl."+this.getClass().getName()+" http://pcmdi3.llnl.gov/thredds/esgcet/catalog.xml THREDDS true|false");
    	System.out.println("Example: java esg.search.publish.impl."+this.getClass().getName()+" http://esg-datanode.jpl.nasa.gov/thredds/esgcet/catalog.xml THREDDS true|false");
    	System.out.println("Example: java esg.search.publish.impl."+this.getClass().getName()+" file:///Users/cinquini/Documents/workspace/esg-search/resources/ORNL-oai_dif.xml OAI true|false");
    	System.out.println("Example: java esg.search.publish.impl."+this.getClass().getName()+" file:///Users/cinquini/Documents/workspace/esg-search/resources/cas_rdf.xml CAS true|false");
    	System.exit(-1);

	}
	
}
