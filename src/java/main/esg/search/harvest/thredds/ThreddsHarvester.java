package esg.search.harvest.thredds;

import java.net.URI;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import thredds.catalog.InvCatalog;
import thredds.catalog.InvCatalogFactory;
import thredds.catalog.InvCatalogRef;
import thredds.catalog.InvDataset;
import thredds.catalog.InvDatasetImpl;
import esg.search.core.Record;
import esg.search.harvest.api.MetadataRepositoryCrawler;
import esg.search.harvest.impl.MetadataHarvester;

/**
 * Implementation of {@link MetadataRepositoryCrawler} for processing a hierarchy of THREDDS catalogs.
 * This class implements the recursive behavior of the THREDDS harvesting process,
 * while delegating the parsing of catalogs and indexing of records to other configurable components.
 */
public class ThreddsHarvester extends MetadataHarvester {
	
	private final ThreddsParserStrategy parser;
		
	private final Log LOG = LogFactory.getLog(this.getClass());
	
	public ThreddsHarvester(final ThreddsParserStrategy parser) {
		this.parser = parser;
	}
	
	/**
	 * Method to crawl a THREDDS catalog located at some URI,
	 * and optionally the whole hierarchy of referenced catalogs.
	 * @param uri : the URI of the starting THREDDS catalog
	 * @param recursive : true to crawl the whole catalog hierarchy
	 */
	public void crawl(final URI catalogURI, boolean recursive) throws Exception {
		
		final InvCatalogFactory factory = new InvCatalogFactory("default", true); // validate=true
		final InvCatalog catalog = factory.readXML(catalogURI);
		final StringBuilder buff = new StringBuilder();
		
		// valid catalog
		if (catalog.check(buff)) {
			
			if (LOG.isInfoEnabled()) LOG.info("Parsing catalog:"+catalogURI.toString());
			for (final InvDataset dataset : catalog.getDatasets()) {
				
				if (dataset instanceof InvCatalogRef) {
					if (recursive) {
						// crawl catalogs recursively
						final URI catalogRef = getCatalogRef(dataset);
						crawl(catalogRef, recursive);
					}
				} else {
					// parse this catalog
					final List<Record> records = parser.parseDataset(dataset);
					// index all resulting records
					for (final Record record : records) notify(record);
				}
			}
			
		// invalid catalog
		} else {
			throw new Exception(buff.toString()); 
		}
		
	}
	
	private URI getCatalogRef(final InvDataset dataset) throws Exception {

		final InvCatalogRef catalogRef = (InvCatalogRef) dataset;
		String uriString = InvDatasetImpl.resolve(dataset, catalogRef.getXlinkHref());
		uriString = uriString.replace("/./", "/");
		uriString = uriString.replace("\\.\\", "\\");
		final URI uri = new URI(uriString);
		uri.normalize();
		return uri;
		
	}
	
}
