package esg.search.query.ws.hessian;

import java.util.Map;

import esg.search.query.api.SearchReturnType;

/**
 * API for querying a remote Search Web Service.
 * To facilitate cross-language communication, 
 * all methods rely on a simplified signature that only contains primitive types or common data structures of primitive types.
 * 
 * @author luca.cinquini
 *
 */
public interface SearchWebService {
	
	/**
	 * Base method to execute the most generic query possible.
	 * 
	 * @param query : the query string (example: "CMIP5" or "timestamp:[2010-10-19T22:00:00Z TO NOW]")
	 * @param type: optional type of returned results
	 * @param constraints : map of facet constraints to filter the query (example: ("variable","tauv"))
	 * @param offset : offset of returned results
	 * @param limit : maximum number of returned results
	 * @param distrib : true to execute a distributed search
	 * @param getResults : true to return results matching the query
	 * @param getFacets : true to return facets matching the query
	 * @param returnType : "XML" or "JSON"
	 * @return
	 */
	String search(final String query, final String type, final Map<String,String[]> constraints,
			      int offset, int limit, boolean distrib, boolean getResults, boolean getFacets, SearchReturnType returnType) throws Exception;
	
	/**
	 * Simplified method to search for records that match a given identifier expression (possibly containing wildcards)
	 * @param idMatch : a string matching the record identifier (example "cmip5.output.PCMDI.pcmdi-test.historical.fx.atmos.fx.r0i0p0" or "cmip5.*")
	 * @param type: optional type to sub-select the results, if a wildcard expression is used
	 * @param offset : offset of returned results
	 * @param limit : maximum number of returned results
	 * @param getResults : true to return results matching the query
	 * @param getFacets : true to return facets matching the query
	 * @param returnType : "XML" or "JSON"	
	 * @return
	 * @throws Exception
	 */
	String searchById(final String idMatch, final String type,
			          int offset, int limit, boolean getResults, boolean getFacets, SearchReturnType returnType) throws Exception;

	/**
	 * Simplified method to search for records that were last updated within a given time range.
	 * Date and times must be specified in ISO8601 format ("YYYYMMDDThh:mm:ssZ") or as special strings: "NOW",...
	 * 
	 * @param fromTimeStamp : the lower limit for the record update timestamp (example: "2010-10-19T22:00:00Z")
	 * @param type: optional type to sub-select the results, if a wildcard expression is used
	 * @param toTimeStamp : the upper limit for the record update timestamp (example: "NOW")
	 * @param offset : offset of returned results
	 * @param limit : maximum number of returned results
	 * @param getResults : true to return results matching the query
	 * @param getFacets : true to return facets matching the query
	 * @param returnType : "XML" or "JSON"
	 * @return
	 * @throws Exception
	 */
	String searchByTimeStamp(final String fromTimeStamp, final String toTimeStamp, final String type,
			                 int offset, int limit, boolean getResults, boolean getFacets, SearchReturnType returnType) throws Exception;

}