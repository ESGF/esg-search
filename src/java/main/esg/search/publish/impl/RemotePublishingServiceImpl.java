package esg.search.publish.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import esg.search.core.Record;
import esg.search.publish.api.LegacyPublishingService;
import esg.search.publish.api.MetadataRepositoryType;
import esg.search.publish.api.PublishingException;
import esg.search.publish.api.PublishingService;
import esg.search.publish.api.RemotePublishingService;
import esg.search.query.api.QueryParameters;
import esg.search.query.api.SearchInput;
import esg.search.query.api.SearchOutput;
import esg.search.query.api.SearchService;
import esg.search.query.impl.solr.SearchInputImpl;
import esg.search.utils.ApplicationContextProvider;

/**
 * Class that allows Hessian invocation of the configured {@link PublishingService} deployed in the Spring application context.
 * The {@link PublishingService} methods simply delegate to the underlying {@link PublishingService} implementation.
 * The {@link LegacyPublishingService} methods execute additional logic to implement the behavior expected by the ESGF publishing application.
 * 
 * Note: this bean is also deployed as part of the Spring application context to allow easy invocation by local main programs,
 * but that is NOT the instance that is invoked by a remote client through the HessianServlet.
 * 
 * @author Luca Cinquini
 *
 */
@Service("remotePublishingService")
public class RemotePublishingServiceImpl implements RemotePublishingService {
    
    // Identifier of PublishingService bean deployed in Spring context.
    private final static String PUBLISHING_SERVICE_BEAN = "securePublishingService";
    
    // Identifier of SearchService bean deployed in Spring context
    // The SearchService is needed to query for all datasets matching a given "instance_id" or "master_id".
    private final static String SEARCH_SERVICE_BEAN = "searchServiceMaster";

    /**
     * For the legacy methods, only harvest THREDDS metadata repositories.
     */
    private final static MetadataRepositoryType METADATA_REPOSITORY_TYPE = MetadataRepositoryType.THREDDS;
    
    /**
     * Always harvest full catalog hierarchy.
     */
    private final static boolean RECURSIVE = true;
    
    private final static String RETURN_VALUE = "SUCCESSFUL";
    
    private final Log LOG = LogFactory.getLog(this.getClass());
    
    /**
     * No-argument constructor.
     */
    public RemotePublishingServiceImpl() {}


    @Override
    public void publish(String uri, String filter, boolean recursive, MetadataRepositoryType metadataRepositoryType, URI schema) throws PublishingException {
        this.getPublishingService().publish(uri, filter, recursive, metadataRepositoryType, schema);
    }

    @Override
    public void unpublish(String uri, String filter, boolean recursive, MetadataRepositoryType metadataRepositoryType) throws PublishingException {
        this.getPublishingService().unpublish(uri, filter, recursive, metadataRepositoryType);
    }

    @Override
    public void unpublish(List<String> ids) throws PublishingException {
        this.getPublishingService().unpublish(ids);
    }
    
    @Override
    public void retract(List<String> ids) throws PublishingException {
        this.getPublishingService().retract(ids);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createDataset(final String parentId, final String threddsURL, final int resursionLevel, final String status) throws PublishingException {
        
        final PublishingService publishingService = this.getPublishingService();
        
        try {
            // Note filter=null since dataset is being created
            publishingService.publish(threddsURL, null, RECURSIVE, METADATA_REPOSITORY_TYPE, null); // schema=null 
            return RETURN_VALUE;
        } catch(PublishingException e) {
            LOG.error(e.getMessage());
            e.printStackTrace();
            throw(e);
        }
        
    }

    /**
     *{@inheritDoc}
     */
    @Override
    public void deleteDataset(final String datasetId, final boolean recursive, final String message) throws PublishingException {
        
    	if (LOG.isInfoEnabled()) LOG.info("Deleting dataset with id="+datasetId);
    	
        final PublishingService publishingService = this.getPublishingService();
        
        final List<String> ids = getDatasetsById(datasetId, null);
        
        publishingService.unpublish(ids);
    	        
    }
    
    /**
     *{@inheritDoc}
     */
    @Override
    public void deleteDatasetSingleDataNode(final String datasetId, final String dataNode, final boolean recursive, final String message) throws PublishingException {
        
    	if (LOG.isInfoEnabled()) LOG.info("Deleting dataset with id="+datasetId+" from data_node="+dataNode);
    	
        final PublishingService publishingService = this.getPublishingService();
        
        final List<String> ids = getDatasetsById(datasetId, dataNode);
        
        publishingService.unpublish(ids);
    	        
    }
    
    /**
     *{@inheritDoc}
     */
    @Override
    public void retractDataset(final String datasetId, final boolean recursive, final String message) throws PublishingException {
        
    	if (LOG.isInfoEnabled()) LOG.info("Retracting dataset with id="+datasetId);
    	
        final PublishingService publishingService = this.getPublishingService();
        
        final List<String> ids = getDatasetsById(datasetId, null);
        
        publishingService.retract(ids);
        
    }
    
    /**
     *{@inheritDoc}
     */
    @Override
    public void retractDatasetSingleDataNode(final String datasetId, final String dataNode, final boolean recursive, final String message) throws PublishingException {
        
    	if (LOG.isInfoEnabled()) LOG.info("Retracting dataset with id="+datasetId+" from data_node="+dataNode);
    	
        final PublishingService publishingService = this.getPublishingService();
        
        final List<String> ids = getDatasetsById(datasetId, dataNode);
        
        publishingService.retract(ids);
        
    }
    
    /**
     * Retrieve all datasets that have their master_id or instance_id equal tio the given id
     * @param datasetId
     * @return
     */
    private List<String> getDatasetsById(final String datasetId, final String dataNode) {
    	
        List<Record> records = new ArrayList<Record>();
        
        // find all datasets matching the given "master_id"
        records.addAll(getDatasetsByIdType(QueryParameters.FIELD_MASTER_ID, datasetId, dataNode));
        
        // find all datasets matching the given "instance_id"
        records.addAll(getDatasetsByIdType(QueryParameters.FIELD_INSTANCE_ID, datasetId, dataNode));

        // delete all matching datasets
        final List<String> ids = new ArrayList<String>();
        for (final Record record : records) {
            ids.add(record.getId());
        }

        return ids;
    	
    }

    /**
     *{@inheritDoc}
     */
    @Override
    public String getPublishingStatus(String operationHandle) {
        return RETURN_VALUE;
    }
    
    /**
     * Utility method to retrieve datasets by matching "master_id" or "version_id".
     * 
     * @param master_id
     */
    private List<Record> getDatasetsByIdType(final String idType, final String idValue, final String dataNode) throws PublishingException {
        
        final SearchService searchService = this.getSearchService();
               
        try {
            final SearchInput input = new SearchInputImpl(QueryParameters.TYPE_DATASET);
            input.setConstraint(idType, idValue);
            input.setDistrib(false);
            
            if (dataNode != null) {
                input.setConstraint("data_node", dataNode);
            }
                                
            // execute query
            final SearchOutput output = searchService.search(input);
            
            return output.getResults();
            
        } catch(Exception e) {
            throw new PublishingException(e.getMessage());
        }
        
    }
    
    /**
     * Retrieves the configured {@link PublishingService} from the Spring application context.
     * @return
     */
    protected PublishingService getPublishingService() {
        return ApplicationContextProvider.getApplicationContext().getBean(PUBLISHING_SERVICE_BEAN, PublishingService.class);
    }
    
    /**
     * Retrieves the configured {@link SearchService} from the Spring application context.
     * @return
     */
    protected SearchService getSearchService() {
        return ApplicationContextProvider.getApplicationContext().getBean(SEARCH_SERVICE_BEAN, SearchService.class);
    }
}
