package esg.search.publish.jaxrs;

import java.net.URL;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import esg.search.publish.api.PublishingService;
import esg.search.publish.impl.solr.SolrRetractor;
import esg.search.publish.security.AuthorizerAdapter;
import esg.search.publish.validation.RecordValidator;

/**
 * RESTful publishing service that targets the master Solr on port 8984.
 * 
 * @author Luca Cinquini
 *
 */
@Path("/")
@Produces("application/xml")
public class PublishResource extends AbstractPublishResource {
    
    @Autowired
    public PublishResource(final @Value("${esg.search.solr.publish.url}") URL url,
                           final @Qualifier("publishingService") PublishingService publishingService,
                           final AuthorizerAdapter authorizer,
                           final @Qualifier("recordValidatorManager") RecordValidator validator,
                           final @Qualifier("retractor") SolrRetractor retractor) throws Exception {
        
    	super(url, publishingService, authorizer, validator, retractor);
        
    }
    
    @GET
    @Path("ping/")
    @Produces("text/plain")
    public String index() {
    	return "ESGF DEFAULT REST Publishing Service";
    }
    
    @GET
    @Path("updateById/")
    public String updateById(
    		@FormParam("core") String core,
    		@FormParam("action") String action,
    		@FormParam("id") String id,
    		@FormParam("field") String field,
    		@FormParam("value") String[] values) {
    	return super.updateById(core, action, id, field, values);
    }
    
    @POST
    @Path("update/")
    public String update(String document) {
    	return super.update(document);    	
    }
    
    @POST
    @Path("publish/")
    public String publish(String record) {
        return super.publish(record);
    }
    
    @POST
    @Path("unpublish/")
    public String unpublish(String record) {
        return super.unpublish(record);
    }
    
    @POST
    @Path("harvest/")
    public String harvest(@FormParam("uri") String uri, 
                          @FormParam("recursive") @DefaultValue("false") boolean recursive, 
                          @FormParam("filter") @DefaultValue("*") String filter,
                          @FormParam("metadataRepositoryType") String metadataRepositoryType,
                          @FormParam("schema") String schema) {
        return super.harvest(uri, recursive, filter, metadataRepositoryType, schema);
    }
    
    @POST
    @Path("unharvest/")
    public String unharvest(@FormParam("uri") String uri, 
                            @FormParam("recursive") @DefaultValue("false") boolean recursive, 
                            @FormParam("filter") @DefaultValue("*") String filter, 
                            @FormParam("metadataRepositoryType") String metadataRepositoryType,
                            @FormParam("schema") String schema) {
        return super.unharvest(uri, recursive, filter, metadataRepositoryType, schema);        
    }
    
    @POST
    @Path("delete/")
    public String delete(@FormParam("id") List<String> ids) {
        return super.delete(ids);
    }
    
    @POST
    @Path("retract/")
    public String retract(@FormParam("id") List<String> ids) {
        return super.retract(ids);
    }
   
}
