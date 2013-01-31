package esg.search.publish.jaxrs;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.impl.ResponseBuilderImpl;
import org.jdom.JDOMException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import esg.search.core.Record;
import esg.search.core.RecordSerializer;
import esg.search.publish.api.MetadataRepositoryType;
import esg.search.publish.api.PublishingException;
import esg.search.publish.api.PublishingService;
import esg.search.publish.impl.solr.SolrClient;
import esg.search.publish.impl.solr.SolrRecordSerializer;
import esg.search.publish.security.AuthorizerAdapter;
import esg.search.publish.validation.RecordValidator;

/**
 * JAXRS Resource that exposes publishing operations (push and pull) through a RESTful API.
 * 
 * @author Luca Cinquini
 *
 */
@Path("/")
@Produces("application/xml")
public class PublishResource {
    
    private final Log LOG = LogFactory.getLog(this.getClass());
    
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
    
    // class used to authorize the publishing calls
    // no authorization takes place if null
    private final AuthorizerAdapter authorizer = null;
        
    /**
     * Constructor is configured to interact with a specific Solr server.
     * @param url
     */
    @Autowired
    public PublishResource(final @Value("${esg.search.solr.publish.url}") URL url,
                           final @Qualifier("publishingService") PublishingService publishingService,
                           final AuthorizerAdapter authorizer,
                           final @Qualifier("recordValidatorManager") RecordValidator validator) throws Exception {
        
        this.solrClient = new SolrClient(url);
        
        this.publishingService = publishingService;
        
        // FIXME
        //this.authorizer = authorizer;
        
        this.validator = validator;
        
        this.serializer = new SolrRecordSerializer();
        
    }
    
    /**
     * Test GET method.
     * @return
     */
    @GET
    @Produces("text/plain")
    public String index() {
        return "ESGF REST Publishing Service";
    }
    
    /**
     * POST push publishing method: pushes XML records to be published to the server.
     * This method authorization is based on the record identifier.
     * 
     * @param record: record to be published encoded as XML/Solr.
     * 
     * @return
     */
    @POST
    @Path("publish/")
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
            if (LOG.isDebugEnabled()) LOG.debug("Number of errors="+errors.size());
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
    @POST
    @Path("unpublish/")
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
    @POST
    @Path("harvest/")
    public String harvest(@FormParam("uri") String uri, 
                          @FormParam("recursive") @DefaultValue("false") boolean recursive, 
                          @FormParam("filter") @DefaultValue("*") String filter,
                          @FormParam("metadataRepositoryType") String metadataRepositoryType,
                          @FormParam("schema") String schema) {
        
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
    @POST
    @Path("unharvest/")
    public String unharvest(@FormParam("uri") String uri, 
                            @FormParam("recursive") @DefaultValue("false") boolean recursive, 
                            @FormParam("filter") @DefaultValue("*") String filter, 
                            @FormParam("metadataRepositoryType") String metadataRepositoryType,
                            @FormParam("schema") String schema) {
        
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
    @POST
    @Path("delete/")
    public String delete(@FormParam("id") List<String> ids) {
        
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
