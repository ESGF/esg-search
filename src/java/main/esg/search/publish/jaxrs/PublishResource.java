package esg.search.publish.jaxrs;

import java.net.URL;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import esg.search.core.Record;
import esg.search.publish.api.MetadataRepositoryType;
import esg.search.publish.api.PublishingService;
import esg.search.publish.impl.solr.SolrClient;
import esg.search.publish.security.AuthorizerAdapter;
import esg.search.publish.validation.CoreRecordValidator;
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
        
    // collaborator that validate records (for push operations)
    public RecordValidator validator;
    
    // service that parses remote metadata repositories
    // (for pull operations)
    private final PublishingService publishingService;
    
    // class used to authorize the publishing calls
    private final AuthorizerAdapter authorizer;
        
    /**
     * Constructor is configured to interact with a specific Solr server.
     * @param url
     */
    @Autowired
    public PublishResource(final @Value("${esg.search.solr.publish.url}") URL url,
                           final @Qualifier("publishingService") PublishingService publishingService,
                           final AuthorizerAdapter authorizer) throws Exception {
        
        this.solrClient = new SolrClient(url);
        
        this.publishingService = publishingService;
        
        this.authorizer = authorizer;
        
        this.validator = new CoreRecordValidator();
        
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
        
        try {
            
            Record obj = validator.validate(record);
            if (LOG.isDebugEnabled()) LOG.debug("Detected record type="+obj.getType());
            
            // authorization
            authorizer.checkAuthorization(obj.getId());
            
            // FIXME: authorize obj
            String request = "<add>"+record+"</add>";
            String response = solrClient.index(request, obj.getType(), true); // commit=true after this record
            return response;
            
        } catch(Exception e) {
            throw newWebApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
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
        
        try {
            
            Record obj = validator.validate(record);
            if (LOG.isDebugEnabled()) LOG.debug("Detected record type="+obj.getType());
            
            // authorization
            authorizer.checkAuthorization(obj.getId());
            
            String response = solrClient.delete(obj.getId());
            return response;
            
        } catch(Exception e) {
            throw newWebApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
        
    }
    
    /**
     * POST pull harvesting method: requests the server to harvest a remote metadata catalog.
     * This method authorization is based on the catalog uri.
     * 
     * @param uri
     * @param filter
     * @param recursive
     * @param metadataRepositoryType
     * @return
     */
    @POST
    @Path("harvest/")
    public String harvest(@FormParam("uri") String uri, 
                          @FormParam("filter") @DefaultValue("*") String filter, 
                          @FormParam("recursive") @DefaultValue("true") boolean recursive, 
                          @FormParam("metadataRepositoryType") String metadataRepositoryType) {
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Harvesting request:");
            LOG.debug("\turi="+uri);
            LOG.debug("\tfilter="+filter);
            LOG.debug("\trecursive="+recursive);
            LOG.debug("\tmetadataRepositoryType="+metadataRepositoryType);
            
        }
        MetadataRepositoryType _metadataRepositoryType = MetadataRepositoryType.valueOf(metadataRepositoryType);
        // FIXME: check _metadataRepositoryType not null
        
        // authorization
        authorizer.checkAuthorization(uri);
        
        publishingService.publish(uri, filter, recursive, _metadataRepositoryType);
        
        return "";
        
    }
    
    /**
     * POST pull unharvesting method: requests the server to unharvest a remote metadata catalog.
     * This method authorization is based on the catalog uri.
     * 
     * @param uri
     * @param filter
     * @param recursive
     * @param metadataRepositoryType
     * @return
     */
    @POST
    @Path("unharvest/")
    public String unharvest(@FormParam("uri") String uri, 
                            @FormParam("filter") @DefaultValue("*") String filter, 
                            @FormParam("recursive") @DefaultValue("true") boolean recursive, 
                            @FormParam("metadataRepositoryType") String metadataRepositoryType) {
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Un-harvesting request:");
            LOG.debug("\turi="+uri);
            LOG.debug("\tfilter="+filter);
            LOG.debug("\trecursive="+recursive);
            LOG.debug("\tmetadataRepositoryType="+metadataRepositoryType);
            
        }
        MetadataRepositoryType _metadataRepositoryType = MetadataRepositoryType.valueOf(metadataRepositoryType);
        // FIXME: check _metadataRepositoryType not null
        
        // authorization
        authorizer.checkAuthorization(uri);
        
        publishingService.unpublish(uri, filter, recursive, _metadataRepositoryType);
        
        return "";
        
    }
    
    /**
     * Push POST deletion method: delete records by specific identifiers.
     * This method authorization is based on the records identifiers.
     * 
     * @param ids
     * @return
     */
    @POST
    @Path("delete/")
    public String delete(@FormParam("id") List<String> ids) {
        
        // authorization
        for (String id : ids) {
            if (LOG.isDebugEnabled()) LOG.debug("Unpublishing id="+id);
            authorizer.checkAuthorization(id);
        }
        
        try {
            String response = solrClient.delete( ids );
            return response;
            
        } catch(Exception e) {
            e.printStackTrace();
            throw newWebApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
                
    }
    
    /**
     * Helper method to build an HTTP exception response with given body content and status code.
     * @param message
     * @param status
     * @return
     */
    private WebApplicationException newWebApplicationException(String message, Response.Status status) {
        
        ResponseBuilderImpl builder = new ResponseBuilderImpl();
        builder.status(status);
        builder.entity("<?xml version=\"1.0\" encoding=\"UTF-8\"?><error>"+message+"</error>");
        Response response = builder.build();
        return new WebApplicationException(response);
        
    }

}
