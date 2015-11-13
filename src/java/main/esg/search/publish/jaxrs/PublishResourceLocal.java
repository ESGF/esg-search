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
import esg.search.publish.security.AuthorizerAdapter;
import esg.search.publish.validation.RecordValidator;

/**
 * JAXRS Resource that executes PUSH/PULL publishing operations versus the "local shard" Solr on port 8982.
 * 
 * @author Luca Cinquini
 *
 */
@Path("/")
@Produces("application/xml")
public class PublishResourceLocal extends PublishResource {
	
    /**
     * Constructor is configured to interact with the local shard Solr server.
     * @param url
     */
    @Autowired
    public PublishResourceLocal(final @Value("${esg.search.solr.local.url}") URL url,
                           final @Qualifier("publishingServiceLocal") PublishingService publishingService,
                           final AuthorizerAdapter authorizer,
                           final @Qualifier("recordValidatorManager") RecordValidator validator) throws Exception {
        
    	super(url, publishingService, authorizer, validator);
        
    }
    
    /**
     * Test GET method.
     * @return
     */
    @GET
    @Path("pingLocal/")
    @Produces("text/plain")
    public String index() {
        return "ESGF LOCAL REST Publishing Service";
    }
    
    /**
     * Publish record to local shard.
     * @param record
     * @return
     */
    @POST
    @Path("publishLocal/")
    public String publish(String record) {
    	return super.publish(record);
    }
    
    /**
     * Unpublish record from local shard.
     * @param record
     * @return
     */
    @POST
    @Path("unpublishLocal/")
    public String unpublish(String record) {
    	return super.unpublish(record);
    }

    /**
     * Harvest a catalog to the local shard.
     */
    @POST
    @Path("harvestLocal/")
    public String harvest(@FormParam("uri") String uri, 
                          @FormParam("recursive") @DefaultValue("false") boolean recursive, 
                          @FormParam("filter") @DefaultValue("*") String filter,
                          @FormParam("metadataRepositoryType") String metadataRepositoryType,
                          @FormParam("schema") String schema) {
    	return super.harvest(uri, recursive, filter, metadataRepositoryType, schema);
    }
    
    /**
     * Unharvest a catalog from the local shard.
     */
    @POST
    @Path("unharvestLocal/")
    public String unharvest(@FormParam("uri") String uri, 
                            @FormParam("recursive") @DefaultValue("false") boolean recursive, 
                            @FormParam("filter") @DefaultValue("*") String filter, 
                            @FormParam("metadataRepositoryType") String metadataRepositoryType,
                            @FormParam("schema") String schema) {
    	return super.unharvest(uri, recursive, filter, metadataRepositoryType, schema);
    }
    
    /**
     * Delete records from the local shard.
     */
    @POST
    @Path("deleteLocal/")
    public String delete(@FormParam("id") List<String> ids) {
    	return super.delete(ids);
    }
    
}
