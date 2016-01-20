package esg.search.publish.jaxrs;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.impl.ResponseBuilderImpl;
import org.jdom.JDOMException;
import org.springframework.util.StringUtils;

import esg.search.core.Record;
import esg.search.core.RecordSerializer;
import esg.search.publish.api.MetadataRepositoryType;
import esg.search.publish.api.MetadataUpdateService;
import esg.search.publish.api.PublishingException;
import esg.search.publish.api.PublishingService;
import esg.search.publish.impl.MetadataUpdateServiceImpl;
import esg.search.publish.impl.UpdateDocumentParser;
import esg.search.publish.impl.solr.SolrClient;
import esg.search.publish.impl.solr.SolrRecordSerializer;
import esg.search.publish.security.AuthorizerAdapter;
import esg.search.publish.validation.RecordValidator;

/**
 * Abstract super-class of RESTful ESGF publishing services.
 * Concrete sub-classes are configured to target a specific Solr index.
 * 
 * @author Luca Cinquini
 *
 */
public abstract class AbstractPublishResource {
    
    private final Log LOG = LogFactory.getLog(this.getClass());
    
    // base Solr URL (example: "http://localhost:8984/solr")
    private URL url;
    
    // client that sends XML requests to the Solr server
    // (for push operations)
    private SolrClient solrClient; 
        
    // collaborator that serializes the records into XML
    RecordSerializer serializer;
    
    // collaborator that validate records (for push operations)
    public RecordValidator validator;
    
    // service that parses remote metadata repositories
    // (for pull operations)
    private final PublishingService publishingService;
    
    // service that updates already published metadata records
    private MetadataUpdateService updateService;
    
    // class used to authorize the publishing calls
    // no authorization takes place if null
    private final AuthorizerAdapter authorizer;
        
    /**
     * Constructor is configured to interact with a specific Solr server.
     * 
     * @param url
     */
    public AbstractPublishResource(
    		final URL url, 
    		final PublishingService publishingService,
    		final AuthorizerAdapter authorizer,
    		final RecordValidator validator) throws Exception {
        
    	this.url = url;
    	
        this.solrClient = new SolrClient(this.url);
        
        this.publishingService = publishingService;
        
        this.authorizer = authorizer;
        
        this.validator = validator;
        
        this.serializer = new SolrRecordSerializer();
        
        this.updateService = new MetadataUpdateServiceImpl(this.authorizer);
        
    }
    
    /**
     * Test GET method.
     * @return
     */
    public String index() {
        return "ESGF REST Publishing Service";
    }
    
    /**
     * POST bulk-update method: parses an XML update document in ESGF syntax,
     * queries Solr for all matching records, 
     * and sends set/add/remove requests to the Solr index.
     * 
     * @param document
     * @param uriInfo
     * @return
     */
    public String update(String document) {
    	
        // validate HTTP request
        if (!StringUtils.hasText(document)) 
            throw newWebApplicationException("Request body must contain the update documemnt", Response.Status.BAD_REQUEST);

    	// parse input document
    	if (LOG.isDebugEnabled()) LOG.debug("Received update document="+document);
    	
    	int numRecordsUpdated = 0;
    	try {
    		UpdateDocumentParser parser = new UpdateDocumentParser(document);
    		
    		String action = parser.getAction();
    		String core = parser.getCore();
    		HashMap<String, Map<String,String[]>> doc = parser.getDoc();
    		    		
    		// execute update
    		numRecordsUpdated = updateService.update(this.url.toString(), core, action, doc);
    		
    	} catch(SecurityException se) {
    		se.printStackTrace();
    		if (LOG.isWarnEnabled()) LOG.warn(se.getMessage());
    		throw newWebApplicationException(se.getMessage(), Response.Status.UNAUTHORIZED);
    		
    	} catch(Exception e) {
    		e.printStackTrace();
    		if (LOG.isWarnEnabled()) LOG.warn(e.getMessage());
    		throw newWebApplicationException(e.getMessage(), Response.Status.BAD_REQUEST);
    	}
    	
    	return newXmlResponse("Number of records updated: "+numRecordsUpdated);
    	
    }
    
    /**
     * POST push publishing method: pushes XML records to be published to the server.
     * This method authorization is based on the record identifier.
     * 
     * @param record: record to be published encoded as XML/Solr.
     * 
     * @return
     */
    public String publish(String record) {
        
        // validate HTTP request
        if (!StringUtils.hasText(record)) 
            throw newWebApplicationException("Request body must contain the record to be published", Response.Status.BAD_REQUEST);
        
        try {       
            
            // deserialize XML into record
            Record obj = serializer.deserialize(record);
            
            // authorization
            if (authorizer!=null) authorizer.checkAuthorization(obj.getId());
            
            // validate record
            List<String> errors = new ArrayList<String>();
            validator.validate(obj, errors);            
            if (LOG.isDebugEnabled()) {
            	LOG.debug("Number of errors="+errors.size());
            	for (String error : errors) {
            		LOG.debug("Error: "+ error);
            	}
            }
            
            if (!errors.isEmpty()) {
                throw newWebApplicationException(errors, Response.Status.BAD_REQUEST);
            }
            
            String request = "<add>"+record+"</add>";
            // ignore response from Solr client
            solrClient.index(request, obj.getType(), true); // commit=true after this record
            return newXmlResponse("Published record: "+obj.getId());
            
        } catch(SecurityException se) {
            // security error
            throw newWebApplicationException(se.getMessage(), Response.Status.UNAUTHORIZED);
            
        } catch(JDOMException je) {
            // XML validation error
            throw newWebApplicationException(je.getMessage(), Response.Status.BAD_REQUEST);
            
        } catch(WebApplicationException we) {
            throw we;
        } catch(Exception e) {
            // all other errors
            e.printStackTrace();
            throw newWebApplicationException(e.getClass().getName()+": "+e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
        
    }
    
    /**
     * POST push unpublishing method: pushes XML records to be unpublished to the server.
     * This method authorization is based on the record identifier.
     * 
     * @param record
     * @return
     */
    public String unpublish(String record) {
        
        // validate HTTP request
        if (!StringUtils.hasText(record)) 
            throw newWebApplicationException("Request body must contain the record to be unpublished", Response.Status.BAD_REQUEST);
        
        try {
            
            // deserialize XML into record
            Record obj = serializer.deserialize(record);
            if (LOG.isDebugEnabled()) LOG.debug("Detected record type="+obj.getType());
            
            
            // authorization
            if (authorizer!=null) authorizer.checkAuthorization(obj.getId());
            
            // ignore response from Solr client
            solrClient.delete(obj.getId()); 
            return newXmlResponse("Unpublished record: "+obj.getId());
         
            
        } catch(SecurityException se) {
            // security error
            throw newWebApplicationException(se.getMessage(), Response.Status.UNAUTHORIZED);
            
        } catch(JDOMException je) {
            // XML validation error
            throw newWebApplicationException(je.getMessage(), Response.Status.BAD_REQUEST);
            
        } catch(Exception e) {
            // all other errors
            throw newWebApplicationException(e.getClass().getName()+": "+e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
        
    }
    
    /**
     * POST pull harvesting method: requests the server to harvest a remote metadata catalog.
     * This method authorization is based on the catalog uri.
     * @param uri: location identifier of remote metadata repository
     * @param recursive: optional boolean to recursively harvest the metadata repository 
     * @param filter: optional filter to sub-select the remote catalogs
     * @param metadataRepositoryType: type of metadata repository, chosen from controlled vocabulary
     * @return
     */
    public String harvest(String uri, 
                          boolean recursive, 
                          String filter,
                          String metadataRepositoryType,
                          String schema) {
        
        try { 
            
            // validate HTTP parameters       
            MetadataRepositoryType _metadataRepositoryType = validateHarvestParameters(uri, filter, recursive, metadataRepositoryType, schema);
            
            // authorization
            if (authorizer!=null) authorizer.checkAuthorization(uri);
            
            // optional schema validation
            URI schemaUri = (StringUtils.hasText(schema) ? new URI(schema) : null);
            
            publishingService.publish(uri, filter, recursive, _metadataRepositoryType, schemaUri);
            
            return newXmlResponse("Harvested uri="+uri);
        
        } catch(SecurityException se) {
            throw newWebApplicationException(se.getMessage(), Response.Status.UNAUTHORIZED);
        } catch(PublishingException pe) {
            throw newWebApplicationException(pe.getMessage(), Response.Status.BAD_REQUEST);
        } catch(URISyntaxException use) {
            throw newWebApplicationException(use.getMessage(), Response.Status.BAD_REQUEST);
        }
        
    }
    
    /**
     * POST pull unharvesting method: requests the server to unharvest a remote metadata catalog.
     * This method authorization is based on the catalog uri.
     * 
     * @param uri: location identifier of remote metadata repository
     * @param recursive: optional boolean to recursively unpublish the metadata repository 
     * @param filter: optional filter to sub-select the remote catalogs
     * @param metadataRepositoryType: type of metadata repository, chosen from controlled vocabulary
     * @return
     */
    public String unharvest(String uri, 
                            boolean recursive, 
                            String filter, 
                            String metadataRepositoryType,
                            String schema) {
        
        try {
            
            // validate HTTP parameters       
            MetadataRepositoryType _metadataRepositoryType = validateHarvestParameters(uri, filter, recursive, metadataRepositoryType, schema);
            
            // authorization
            if (authorizer!=null) authorizer.checkAuthorization(uri);
            
            publishingService.unpublish(uri, filter, recursive, _metadataRepositoryType);
            
            return newXmlResponse("Unharvested uri="+uri);
        
        } catch(SecurityException se) {
            throw newWebApplicationException(se.getMessage(), Response.Status.UNAUTHORIZED);
        }
        
    }
    
    /**
     * Push POST deletion method: delete records by specific identifiers.
     * This method authorization is based on the records identifiers.
     * 
     * @param id: identifiers of records to be deleted (one or more)
     * @return
     */
    public String delete(List<String> ids) {
        
        // validate HTTP parameters
        if (ids.size()==0) 
            throw newWebApplicationException("Missing mandatory parameter 'id'", Response.Status.BAD_REQUEST);
        
        try {
        
            // authorization
            if (authorizer!=null) {
                for (String id : ids) {
                    if (LOG.isDebugEnabled()) LOG.debug("Unpublishing id="+id);
                    authorizer.checkAuthorization(id);
                }
            }
        
            // ignore response from Solr client
            solrClient.delete( ids );
            List<String> messages = new ArrayList<String>();
            for (String id : ids) messages.add("Deleted id: "+id);
            return newXmlResponse(messages);
            
        } catch(SecurityException se) {
            throw newWebApplicationException(se.getMessage(), Response.Status.UNAUTHORIZED);

        } catch(Exception e) {
            e.printStackTrace();
            throw newWebApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
                
    }
    
    /**
     * Method to validate the parameters needed for a harvesting/unharvesing operation.
     * @param uri
     * @param metadataRepositoryType
     * @return the metadataRepositoryType converted to enumeration value
     */
    private MetadataRepositoryType validateHarvestParameters(String uri, String filter, boolean recursive, 
                                                             String metadataRepositoryType, String schema) {
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Harvesting request:");
            LOG.debug("\turi="+uri);
            LOG.debug("\tfilter="+filter);
            LOG.debug("\trecursive="+recursive);
            LOG.debug("\tmetadataRepositoryType="+metadataRepositoryType);
            LOG.debug("\tschema="+schema);
            
        }
        
        if (!StringUtils.hasText(uri)) 
            throw newWebApplicationException("Missing mandatory parameter 'uri'", Response.Status.BAD_REQUEST);
        
        if (!StringUtils.hasText(metadataRepositoryType)) 
            throw newWebApplicationException("Missing mandatory parameter 'metadataRepositoryType'", Response.Status.BAD_REQUEST);
        
        MetadataRepositoryType _metadataRepositoryType = null;       
        try {
            _metadataRepositoryType = MetadataRepositoryType.valueOf(metadataRepositoryType);
        } catch(IllegalArgumentException e) {
           throw newWebApplicationException("Invalid value for 'metadataRepositoryType': "+metadataRepositoryType, 
                                            Response.Status.BAD_REQUEST);
        }
        
        return _metadataRepositoryType;
           
    }
    
    /**
     * Helper method to build an HTTP exception response with given body content and status code.
     * @param message
     * @param status
     * @return
     */
    private WebApplicationException newWebApplicationException(String message, Response.Status status) {
        
        return newWebApplicationException( Arrays.asList( new String[]{ message } ), status);
                
    }
    
    /**
     * Helper method to build an HTTP exception response with given body content and status code.
     * @param message
     * @param status
     * @return
     */
    private WebApplicationException newWebApplicationException(List<String> messages, Response.Status status) {
        
        // assemble all messages
        StringBuilder sb = new StringBuilder();
        for (String message : messages) {
            sb.append("<message>"+message+"</message>");
        }
        
        // embed messages in response
        ResponseBuilderImpl builder = new ResponseBuilderImpl();
        builder.status(status);
        builder.entity("<?xml version=\"1.0\" encoding=\"UTF-8\"?><response status=\"error\">"+sb.toString()+"</response>").type("application/xml");
        Response response = builder.build();
        return new WebApplicationException(response);

        
    }

    private String newXmlResponse(String message) {
        return newXmlResponse( Arrays.asList( new String[] { message }));
    }
    
    private String newXmlResponse(List<String> messages) {
        
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><response status=\"success\">");
        
        for (String message : messages) {
            sb.append("<message>"+message+"</message>");
        }

        sb.append("</response>");
        return sb.toString();
        
    }

}
