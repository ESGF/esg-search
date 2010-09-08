package esg.search.query.impl.solr;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import esg.search.query.api.Facet;
import esg.search.query.api.FacetProfile;
import esg.search.query.api.SearchInput;
import esg.search.query.api.SearchOutput;
import esg.search.query.api.SearchService;
import esg.search.utils.HttpClient;

/**
 * Implementation of {@link SearchService} based on an Apache-Solr back-end.
 */
public class SearchServiceImpl implements SearchService {

	/**
	 * The facet profile specific to this application or domain.
	 */
	private final FacetProfile facetProfile;
	
	/**
	 * The base URL of the Apache-Sol server.
	 */
	private final URL url;

	/**
	 * The client used to communicate with the Solr server via its REST API.
	 */
	private final HttpClient httpClient = new HttpClient();

	/**
	 * The parser used to parse the XML output from the server.
	 */
	final SolrXmlParser xmlParser = new SolrXmlParser();

	private static final Log LOG = LogFactory.getLog(SearchServiceImpl.class);

	/**
	 * Constructor with mandatory arguments.
	 * 
	 * @param facetProfile
	 * @param url
	 * @throws MalformedURLException
	 */
	public SearchServiceImpl(final FacetProfile facetProfile, final String url) throws MalformedURLException {
		this.facetProfile = facetProfile;
		this.url = new URL(url);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public SearchOutput getResults(final SearchInput input) throws Exception {
		
		// formulate HTTP request
		final SolrUrlBuilder builder = new SolrUrlBuilder(url);
		builder.setSearchInput(input);
		final URL request = builder.buildSelectUrl();		
		
		// execute HTTP request
		final String response = httpClient.doGet(request);
		
		// parse XML response
		final SearchOutput output = xmlParser.parseResults(response);
		return output;
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Map<String, Facet> getFacets(SearchInput input) throws Exception {
		
		// formulate HTTP request
		final SolrUrlBuilder builder = new SolrUrlBuilder(url);
		builder.setSearchInput(input);
		//builder.setFacets(facetProfile.getTopLevelFacets().keySet());
		builder.setFacets(input.getFacets());
		final URL request = builder.buildSelectUrl();		

		// execute HTTP request
		final String response = httpClient.doGet(new URL(request.toString()));

		// parse XML response
		final Map<String, Facet> facets = xmlParser.parseFacets(response, input);
		
		return facets;
		
	}

}
