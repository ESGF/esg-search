/*******************************************************************************
 * Copyright (c) 2010 Earth System Grid Federation
 * ALL RIGHTS RESERVED. 
 * U.S. Government sponsorship acknowledged.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of the <ORGANIZATION> nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package esg.search.harvest.thredds;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import thredds.catalog.InvCatalog;
import thredds.catalog.InvCatalogFactory;
import thredds.catalog.InvCatalogRef;
import thredds.catalog.InvDataset;
import thredds.catalog.InvDatasetImpl;
import esg.search.core.Record;
import esg.search.harvest.api.MetadataRepositoryCrawler;
import esg.search.harvest.api.RecordProducer;

/**
 * Implementation of {@link MetadataHarvester} for processing a hierarchy of THREDDS catalogs.
 * This class implements the recursive behavior of the THREDDS harvesting process,
 * while delegating the parsing of catalogs and indexing of records to other configurable components.
 * Additionally, while crawling a hierarchy of catalogs, only the latest version records will be harvested.
 */
public class ThreddsHarvester implements MetadataRepositoryCrawler {
	
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
	public void crawl(final URI catalogURI, boolean recursive, final RecordProducer callback) throws Exception {
		
		final InvCatalogFactory factory = new InvCatalogFactory("default", true); // validate=true
		final InvCatalog catalog = factory.readXML(catalogURI);
		final StringBuilder buff = new StringBuilder();
		
		// map containing latest version of records.
		final Map<String, Record> vrecords = new HashMap<String,Record>();
		
		// valid catalog
		if (catalog.check(buff)) {
			
			if (LOG.isInfoEnabled()) LOG.info("Parsing catalog:"+catalogURI.toString());
			for (final InvDataset dataset : catalog.getDatasets()) {
				
				if (dataset instanceof InvCatalogRef) {
					if (recursive) {
						// crawl catalogs recursively
						final URI catalogRef = getCatalogRef(dataset);
						crawl(catalogRef, recursive, callback);
					}
				} else {
					// parse this catalog
					final List<Record> records = parser.parseDataset(dataset);
					// index all resulting records (latest version only)
					for (final Record record : records) {
						System.out.println("indexing record="+record.getId());
						if (vrecords.get(record.getId())==null
							|| vrecords.get(record.getId()).getVersion()<record.getVersion()) {
							callback.notify(record);
							vrecords.put(record.getId(), record);
						} 
					}
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
