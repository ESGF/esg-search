package esg.search.ws.hessian.client;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import esg.search.query.api.SearchInput;
import esg.search.query.api.SearchOutput;
import esg.search.query.api.SearchService;
import esg.search.query.impl.solr.SearchInputImpl;


/**
 * Example client for a querying a remote metadata search service.
 * This client relies on Spring client support for the Hessian protocol.
 * @author luca.cinquini
 *
 */
public class SearchWebServiceClient {
		
	public static void main(final String[] args) throws Exception {
		
        final ApplicationContext ctxt = new ClassPathXmlApplicationContext("classpath:esg/search/ws/hessian/client/ws-hessian-client-config.xml");
        final SearchService searchService = (SearchService)ctxt.getBean("searchWebServiceProxy");
	    
	    //final SearchWebServiceClient self = new SearchWebServiceClient();
	    
        // all documents
	    final SearchInput input = new SearchInputImpl();
	    input.addFacet("cf_variable");
	    input.addFacet("variable");
	    input.addFacet("experiment");
	   
	    // All documents that changed anywhere in the system since sometimes today
	    //input.addConstraint("timestamp", "[2010-10-19T22:00:00Z TO NOW]");	   
	    //input.setText("timestamp:[2010-10-19T22:00:00Z TO NOW]");
	    
	    // Single document with given id
	    //input.addConstraint("id", "cmip5.output.PCMDI.pcmdi-test.historical.fx.atmos.fx.r0i0p0");
	    input.setText("id:cmip5.output.PCMDI.pcmdi-test.historical.fx.atmos.fx.r0i0p0");
	    
	    // All documents with wildcard id
	    //input.setText("id:pcmdi.*");
	    
	    final SearchOutput output = searchService.search(input, true, true);
	    System.out.println( output.toString() );
	    
		
	}
	

}
