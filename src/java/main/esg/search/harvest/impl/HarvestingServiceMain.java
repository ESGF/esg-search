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
