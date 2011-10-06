package esg.search.ws.hessian.client;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import esg.search.query.api.SearchReturnType;
import esg.search.query.ws.hessian.SearchWebService;


/**
 * Example client for a querying a remote metadata search service.
 * This client relies on Spring client support for the Hessian protocol.
 * @author luca.cinquini
 *
 */
public class SearchWebServiceClient {
		
	public static void main(final String[] args) throws Exception {
		
        final ApplicationContext ctxt = new ClassPathXmlApplicationContext("classpath:esg/search/ws/hessian/client/ws-hessian-client-config.xml");
        final SearchWebService searchWebService = (SearchWebService)ctxt.getBean("searchWebServiceProxy");
	    	    
	    // fixed search parameters
	    final int offset = 0;
	    final int limit = 10;
	    final boolean getResults = true;
	    final boolean getFacets = true;
	    final SearchReturnType returnType = SearchReturnType.XML;
	    final Map<String,String[]> constraints = new HashMap<String,String[]>();
	    
	    // return all documents
	    String type = null; 
	    String text = "*"; // note: empty string has same effect
	    String xml = searchWebService.search(text, type, constraints, offset, limit, true, getResults, getFacets, returnType);
	    
	    // return documents matching a string and some facet constraints
	    text = "CSIRO";
	    constraints.put("cf_variable", new String[]{"Maximum Daily Surface Air Temperature"});
	    constraints.put("time_frequency", new String[]{"day"});
	    xml = searchWebService.search(text, type, constraints, offset, limit, true, getResults, getFacets, returnType);
	   
	    // specific search by id
	    String id = "pcmdi.ipcc4.CSIRO.csiro_mk3_0.20c3m.day.atm.run1";
	    type = "Dataset";
	    xml = searchWebService.searchById(id, type, offset, limit, getResults, getFacets, returnType);
	    
	    // search on wildcard ids
	    id = "pcmdi.*";
	    xml = searchWebService.searchById(id, type, offset, limit, getResults, getFacets, returnType);
	    
	    // all documents that changed in October
	    String startTimeStamp = "2010-10-01T00:00:00Z";
	    String stopTimeStamp = "2010-10-31T23:59:59Z";
	    xml = searchWebService.searchByTimeStamp(startTimeStamp, stopTimeStamp, type, offset, limit, getResults, getFacets, returnType);
	    
	    // all documents that changed since October
	    stopTimeStamp = "NOW";
	    xml = searchWebService.searchByTimeStamp(startTimeStamp, stopTimeStamp, type, offset, limit, getResults, getFacets, returnType);
	    
	    System.out.println(xml);
		
	}
	

}
