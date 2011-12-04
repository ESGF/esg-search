package esg.search.query.ws.rest;

import java.io.Serializable;

import esg.search.query.impl.solr.SearchInputImpl;

/**
 * Command bean to execute the automatic binding of HTTP request parameters used by the {@link SearchController}.
 * Note: currently this bean does not contain any properties that are not in its superclass, and may be removed in the future.
 * 
 * @author luca.cinquini
 *
 */
public class SearchCommand extends SearchInputImpl implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public SearchCommand() {
		super();
	}

}
