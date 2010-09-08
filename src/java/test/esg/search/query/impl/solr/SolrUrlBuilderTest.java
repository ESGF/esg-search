package esg.search.query.impl.solr;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import esg.search.query.api.SearchInput;

/**
 * Test class for {@link SolrUrlBuilder}.
 */
public class SolrUrlBuilderTest {
	
	private final static String SOLR_URL = "http://localhost:8983/solr";
	private SolrUrlBuilder solrUrlBuilder;
	
	@Before
	public void setup() throws MalformedURLException {
		solrUrlBuilder = new SolrUrlBuilder(new URL(SOLR_URL));
	}
	
	@Test
	public void testBuildUpdateUrl() throws Exception {
		
		// commit=false
		URL url = solrUrlBuilder.buildUpdateUrl(false);
		Assert.assertEquals(SOLR_URL+"/update", url.toString());
		
		// commit=true
		url = solrUrlBuilder.buildUpdateUrl(true);
		Assert.assertEquals(SOLR_URL+"/update?commit=true", url.toString());		
		
	}
	
	@Test
	public void testBuildSelectUrl() throws Exception {
		
		// query default field, match all documents
		final SearchInput input = new SearchInputImpl();
		solrUrlBuilder.setSearchInput(input);
		URL url = solrUrlBuilder.buildSelectUrl();
		Assert.assertEquals(SOLR_URL+"/select/?indent=true", url.toString());
		
		// query default field, specify results type as query filter
		input.setType("Dataset");
		url = solrUrlBuilder.buildSelectUrl();
		Assert.assertEquals(SOLR_URL+"/select/?indent=true&fq=type%3A%22Dataset%22", url.toString());
		
		// query default field, use query filter for results type, match text
		input.setText("atmospheric data");
		url = solrUrlBuilder.buildSelectUrl();
		Assert.assertEquals(SOLR_URL+"/select/?indent=true&q=atmospheric+data&fq=type%3A%22Dataset%22", url.toString());

		// query default field, use query filter for results type, match text, retrieve all facets
		final List<String> facets = Arrays.asList( new String[]{ "facet1", "facet2" } );
		solrUrlBuilder.setFacets(facets);
		url = solrUrlBuilder.buildSelectUrl();
		Assert.assertEquals(SOLR_URL+"/select/?indent=true&q=atmospheric+data&fq=type%3A%22Dataset%22&facet.field=facet1&facet.field=facet2", url.toString());
		
		// query default field, use query filter for results type, match text, use facet constraint, retrieve all facets
		input.addConstraint("facet1", "value1");
		url = solrUrlBuilder.buildSelectUrl();
		Assert.assertEquals(SOLR_URL+"/select/?indent=true&q=atmospheric+data&fq=type%3A%22Dataset%22&fq=facet1%3A%22value1%22&facet.field=facet1&facet.field=facet2", url.toString());
		
	}

}
