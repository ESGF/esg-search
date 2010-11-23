package esg.search.query.ws.rest;

import java.io.Serializable;

import esg.search.query.api.SearchReturnType;
import esg.search.query.impl.solr.SearchInputImpl;

/**
 * Command bean to execute the automatic binding of HTTP request parameters used by the {@link SearchRestController}.
 * This bean (including its superclass) contains default values for all of the optional HTTP request parameters.
 * 
 * @author luca.cinquini
 *
 */
public class SearchRestCommand extends SearchInputImpl implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public SearchRestCommand() {
		super();
	}
	
	/**
	 * Return results by default.
	 */
	private boolean results = true;
	
	/**
	 * Return facets by default.
	 */
	private boolean facets = true;
	
	
	/**
	 * Return XML by default.
	 */
	private SearchReturnType back = SearchReturnType.XML;

	public boolean isResults() {
		return results;
	}


	public void setResults(boolean results) {
		this.results = results;
	}


	public boolean isFacets() {
		return facets;
	}


	public void setFacets(boolean facets) {
		this.facets = facets;
	}


	public SearchReturnType getBack() {
		return back;
	}


	public void setBack(SearchReturnType back) {
		this.back = back;
	}

}
