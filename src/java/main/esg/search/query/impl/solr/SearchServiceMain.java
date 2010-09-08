package esg.search.query.impl.solr;

import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import esg.search.query.api.Facet;
import esg.search.query.api.FacetProfile;
import esg.search.query.api.SearchInput;
import esg.search.query.api.SearchOutput;
import esg.search.query.api.SearchService;

public class SearchServiceMain {
	
	private static String[] configLocations = new String[] { "classpath:esg/search/config/search-context.xml" };
	private static final Log LOG = LogFactory.getLog(SearchServiceMain.class);
	
	public static void main(String[] args) throws Exception {
		
		// load instance from Spring configuration
		final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(configLocations);
		
		final SearchService searchService = (SearchService) context.getBean("searchService");
		
		final FacetProfile facetProfile = (FacetProfile)context.getBean("facetProfile");
				
		// test unconstrained facets
		final SearchInput input = new SearchInputImpl();
		input.setFacets(new ArrayList<String>(facetProfile.getTopLevelFacets().keySet()));
		
		Map<String, Facet> facets = searchService.getFacets(input);
		for (final Facet facet : facets.values()) {
			LOG.info(facet.toString());
		}
		
		// text query
		LOG.info("\nQUERY #1");
		input.setText("sresb1");
		SearchOutput output = searchService.getResults(input);
		LOG.info(output.toString());

		// text + 1 facet query
		LOG.info("\nQUERY #2");
		input.addConstraint("time_frequency", "3hr");
		output = searchService.getResults(input);
		LOG.info(output.toString());
		
		// text + 2 facets query
		LOG.info("\nQUERY #3");
		input.addConstraint("realm", "atm");
		output = searchService.getResults(input);
		LOG.info(output.toString());
		
		// text + 3 facets query
		LOG.info("\nQUERY #4");
		input.addConstraint("cf_variable", "Surface Downwelling Longwave Radiation");
		output = searchService.getResults(input);
		LOG.info(output.toString());
		
		// constrained facets
		LOG.info("\nQUERY #5");
		facets = searchService.getFacets(input);
		for (final Facet facet : facets.values()) {
			LOG.info(facet.toString());
		}
		
		// empty query for results
		LOG.info("\nQUERY #6");
		input.addConstraint("frequency", "Daily");
		output = searchService.getResults(input);
		LOG.info(output.toString());
		
		// empty query for facets
		LOG.info("\nQUERY #7");
		facets = searchService.getFacets(input);
		for (final Facet facet : facets.values()) {
			LOG.info(facet.toString());
		}

		
	}

	

}
