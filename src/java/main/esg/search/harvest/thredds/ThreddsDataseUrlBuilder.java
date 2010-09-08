package esg.search.harvest.thredds;

import thredds.catalog.InvDataset;

/**
 * Interface for building the end-point URL
 * of a search record generated from a THREDDS dataset.
 */
public interface ThreddsDataseUrlBuilder {

	/**
	 * Method to generate the search record URL.
	 * @param dataset : the THREDDS dataset usedf to generate the search record.
	 * @return
	 */
	String buildUrl(InvDataset dataset);
	
}
