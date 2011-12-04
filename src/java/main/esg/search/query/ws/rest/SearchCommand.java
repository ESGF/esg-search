package esg.search.query.ws.rest;

import java.io.Serializable;

import esg.search.query.impl.solr.SearchInputImpl;

/**
 * Command bean to execute the automatic binding of HTTP request parameters used by the {@link SearchController}.
 * This bean (including its superclass) contains default values for all of the optional HTTP request parameters.
 * 
 * @author luca.cinquini
 *
 */
public class SearchCommand extends SearchInputImpl implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public SearchCommand() {
		super();
	}
	
	/**
	 * Return results by default.
	 */
	private boolean results = true;
				    	

}
