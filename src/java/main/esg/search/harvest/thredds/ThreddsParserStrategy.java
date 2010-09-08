package esg.search.harvest.thredds;

import java.util.List;

import thredds.catalog.InvDataset;
import esg.search.core.Record;

/**
 * Interface that specify the strategy for parsing a THREDDS dataset into a list of search records.
 */
public interface ThreddsParserStrategy {

	/**
	 * Method to parse a THREDDS dataset and return an ordered list of search records
	 * @param dataset
	 * @return
	 */
	List<Record> parseDataset(InvDataset dataset) throws Exception;
	
}
