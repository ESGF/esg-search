package esg.search.publish.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import esg.search.core.Record;
import esg.search.publish.api.LegacyPublishingService;
import esg.search.publish.api.MetadataRepositoryType;
import esg.search.publish.api.PublishingService;
import esg.search.query.api.QueryParameters;
import esg.search.query.api.SearchInput;
import esg.search.query.api.SearchOutput;
import esg.search.query.api.SearchService;
import esg.search.query.impl.solr.SearchInputImpl;

/**
 * Implementation of {@link LegacyPublishingService} that delegates all functionality
 * to the underlying {@link PublishingService}.
 * 
 * @author luca.cinquini
 *
 */
@Service("legacyPublishingService")
public class LegacyPublishingServiceImpl implements LegacyPublishingService {
	
	private final PublishingService publishingService;
	
	/**
     * Service needed to query for all datasets matching a given "instance_id" or "master_id".
     */
    private final SearchService searchService;
	
	/**
	 * Only harvest THREDDS metadata repositories.
	 */
	private final static MetadataRepositoryType METADATA_REPOSITORY_TYPE = MetadataRepositoryType.THREDDS;
	
	/**
	 * Always harvest full catalog hierarchy.
	 */
	private final static boolean RECURSIVE = true;
	
	private final static String RETURN_VALUE = "SUCCESSFUL";
	
	private final Log LOG = LogFactory.getLog(this.getClass());
	
	@Autowired
	public LegacyPublishingServiceImpl(final @Qualifier("securePublishingService") PublishingService publishingService,
	                                   final @Qualifier("searchService2") SearchService searchService) {
		this.publishingService = publishingService;
		this.searchService = searchService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String createDataset(final String parentId, final String threddsURL, final int resursionLevel, final String status) throws Exception {
		
	    try {
	        this.publishingService.publish(threddsURL, RECURSIVE, METADATA_REPOSITORY_TYPE);
	        return RETURN_VALUE;
	    } catch(Exception e) {
	        e.printStackTrace();
	        throw e;
	    }
		
	}

	/**
	 *{@inheritDoc}
	 */
	@Override
	public void deleteDataset(final String datasetId, final boolean recursive, final String message) throws Exception {
		
	    try {
	        
	        List<Record> records = new ArrayList<Record>();
	        
    	    // find all datasets matching the given "master_id"
    	    records.addAll(getDatasetsByIdType(QueryParameters.FIELD_MASTER_ID, datasetId));
    	    
    	    // find all datasets matching the given "instance_id"
    	    records.addAll(getDatasetsByIdType(QueryParameters.FIELD_INSTANCE_ID, datasetId));
    	    
    	    // delete all matching datasets
    		final List<String> ids = new ArrayList<String>();
    		for (final Record record : records) {
    		    ids.add(record.getId());
    		    if (LOG.isInfoEnabled()) LOG.info("Deleting dataset with id="+record.getId());
    		}
    		this.publishingService.unpublish(ids);
    		
	    } catch(Exception e) {
	        e.printStackTrace();
	        throw e;
	    }
		
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
    private List<Record> getDatasetsByIdType(final String idType, final String idValue) throws Exception {
        
        final SearchInput input = new SearchInputImpl(QueryParameters.TYPE_DATASET);
        input.setConstraint(idType, idValue);
        input.setDistrib(false);
                
        // execute query
        final SearchOutput output = searchService.search(input);
        
        return output.getResults();
        
    }
	

}
