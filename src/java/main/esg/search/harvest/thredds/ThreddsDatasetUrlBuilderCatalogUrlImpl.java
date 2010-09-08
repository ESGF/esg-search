package esg.search.harvest.thredds;

import thredds.catalog.InvDataset;

/**
 * Implementation of {@link ThreddsDataseUrlBuilder} 
 * that simply returns the THREDDS catalog URL.
 */
public class ThreddsDatasetUrlBuilderCatalogUrlImpl implements ThreddsDataseUrlBuilder {

	/**
	 * {@inheritDoc}
	 */
	public String buildUrl(final InvDataset dataset) {
		return dataset.getCatalogUrl();
	}

}
