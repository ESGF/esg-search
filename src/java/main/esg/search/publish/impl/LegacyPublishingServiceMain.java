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

import org.springframework.context.support.ClassPathXmlApplicationContext;

import esg.search.publish.api.LegacyPublishingService;

/**
 * Main class to run the {@link LegacyPublishingService}.
 * 
 * @author luca.cinquini
 *
 */
public class LegacyPublishingServiceMain {
	
	
    private static String[] configLocations = new String[] { "classpath:esg/search/config/application-context.xml" };
    
    /**
     * Main method loads the proper web service to invoke from the Spring context.
     * 
     * @param args
     * @throws Exception
     */
	public static void main(String[] args) throws Exception {
		
	    final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(configLocations);
	    final LegacyPublishingService publishingService = (LegacyPublishingService)context.getBean("legacyPublishingService");
	    
	    final LegacyPublishingServiceMain self = new LegacyPublishingServiceMain();
	    self.run(publishingService, args);
	    
	}
	
	/**
	 * Method to execute the web service invocation.
	 * 
	 * @param publishingService
	 * @param args
	 * @throws Exception
	 */
	protected void run(final LegacyPublishingService publishingService, final String[] args) throws Exception {
	    
	    if (args.length!=2) {
	    	exit();
	    }

	    // publish
	    if (args[0].equals("publish")) {
	    	
	    	final String url = args[1];
	    	publishingService.createDataset(null, url, 0, null);
	    
	    	// unpublish
	    } else if (args[0].equals("unpublish")) {
	    	
	    	final String datasetId = args[1];
	    	publishingService.deleteDataset(datasetId, false, null);
	    	
	    // invocation error
	    } else {
	    	System.out.println("Operation not supported: "+args[0]);
	    	exit();
	    }
		
	}
	
	/**
	 * Method to indicate usage and exit the program.
	 */
	protected void exit() {
		
    	System.out.println("Usage #1: to publish a THREDDS catalog: ");
    	System.out.println("          java esg.search.publish.impl."+this.getClass().getName()+" publish <THREDDS URL>");
    	System.out.println("          java esg.search.publish.impl."+this.getClass().getName()+" publish file:///Users/cinquini/Documents/workspace/esg-search/resources/pcmdi.ipcc4.GFDL.gfdl_cm2_0.picntrl.mon.land.run1.v1.xml");

    	System.out.println("Usage #2: to unpublish a single dataset:");
    	System.out.println("          java esg.search.publish.impl."+this.getClass().getName()+" unpublish <id>");
    	System.out.println("          java esg.search.publish.impl."+this.getClass().getName()+" unpublish nasa.jpl.tes.monthly");

	    System.exit(-1);

	}
	
}
