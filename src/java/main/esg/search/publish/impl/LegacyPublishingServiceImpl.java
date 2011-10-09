package esg.search.publish.impl;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import esg.search.publish.api.LegacyPublishingService;
import esg.search.publish.api.MetadataRepositoryType;
import esg.search.publish.api.PublishingService;

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
	 * Only harvest THREDDS metadata repositories.
	 */
	private final static MetadataRepositoryType METADATA_REPOSITORY_TYPE = MetadataRepositoryType.THREDDS;
	
	/**
	 * Always harvest full catalog hierarchy.
	 */
	private final static boolean RECURSIVE = true;
	
	private final static String RETURN_VALUE = "SUCCESSFUL";
	
	@Autowired
	public LegacyPublishingServiceImpl(final @Qualifier("securePublishingService") PublishingService publishingService) {
		this.publishingService = publishingService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String createDataset(final String parentId, final String threddsURL, final int resursionLevel, final String status) throws Exception {
		
		this.publishingService.publish(threddsURL, RECURSIVE, METADATA_REPOSITORY_TYPE);
		return RETURN_VALUE;
		
	}

	/**
	 *{@inheritDoc}
	 */
	@Override
	public void deleteDataset(final String datasetId, final boolean recursive, final String message) throws Exception {
		
		final List<String> ids = Arrays.asList(new String[] { datasetId } );
		this.publishingService.unpublish(ids);
		
	}

	/**
	 *{@inheritDoc}
	 */
	@Override
	public String getPublishingStatus(String operationHandle) throws Exception {
		return RETURN_VALUE;
	}
	
	
	

}
