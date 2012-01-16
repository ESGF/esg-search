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
package esg.search.publish.thredds;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import thredds.catalog.InvCatalog;
import thredds.catalog.InvCatalogFactory;
import thredds.catalog.InvCatalogRef;
import thredds.catalog.InvDataset;
import thredds.catalog.InvDatasetImpl;
import esg.search.core.Record;
import esg.search.core.RecordHelper;
import esg.search.publish.api.MetadataRepositoryCrawler;
import esg.search.publish.api.MetadataRepositoryCrawlerListener;
import esg.search.publish.api.MetadataRepositoryType;
import esg.search.publish.api.RecordProducer;
import esg.search.query.api.QueryParameters;
import esg.search.query.api.SearchInput;
import esg.search.query.api.SearchOutput;
import esg.search.query.api.SearchService;
import esg.search.query.impl.solr.SearchInputImpl;

/**
 * Implementation of {@link MetadataRepositoryCrawler} for processing a hierarchy of THREDDS catalogs.
 * This class implements the recursive behavior of the THREDDS harvesting process,
 * while delegating the parsing of catalogs and indexing of records to other configurable components.
 * Additionally, while crawling a hierarchy of catalogs, only the latest version records will be harvested.
 */
@Service("metadataRepositoryCrawler")
public class ThreddsCrawler implements MetadataRepositoryCrawler {
	
    /**
     * Class responsible for parsing each single Thredds catalog.
     */
	private final ThreddsParserStrategy parser;
	
	/**
	 * Service needed to query existing metadata storage for latest versions fo records,
	 * before ingesting new ones.
	 */
	private final SearchService searchService;
	
	private MetadataRepositoryCrawlerListener listener = null;
		
	private final Log LOG = LogFactory.getLog(this.getClass());
	
	/**
	 * Note that the constructor uses the secondary search service, that queries the master Solr instance where records are published.
	 * @param parser
	 * @param searchService
	 */
	@Autowired
	public ThreddsCrawler(final ThreddsParserStrategy parser, final @Qualifier("searchService2") SearchService searchService) {
		this.parser = parser;
		this.searchService = searchService;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public MetadataRepositoryType supports() {
		return MetadataRepositoryType.THREDDS;
	}
	
	/**
	 * Method to crawl a THREDDS catalog located at some URI,
	 * and optionally the whole hierarchy of referenced catalogs.
	 * @param uri : the URI of the starting THREDDS catalog
	 * @param recursive : true to crawl the whole catalog hierarchy
	 * @param publish: true to publish, false to unpublish
	 */
	public void crawl(final URI catalogURI, boolean recursive, final RecordProducer callback, boolean publish) throws Exception {
		
	    try {
	        
	        // parse THREDDS catalog
	        if (LOG.isInfoEnabled()) LOG.info("Parsing catalog: "+catalogURI.toString());
	        final InvCatalog catalog = parseCatalog(catalogURI.toString());
							
			for (final InvDataset dataset : catalog.getDatasets()) {
				
				if (dataset instanceof InvCatalogRef) {
				    
					if (recursive) {
						// crawl catalogs recursively
						final URI catalogRef = getCatalogRef(dataset);
						try {
						    crawl(catalogRef, recursive, callback, publish);
						} catch(Exception e) {
						    // print error from nested invocation
						    LOG.warn("Error parsing catalog: "+catalogRef.toString());
						    LOG.warn(e.getMessage());
						}
					}
					
				} else if (dataset instanceof InvDatasetImpl) {
				    
                    // list or previous records to be republished
                    final List<Record> _records = new ArrayList<Record>();
				    
					// list of records from this catalog
					final List<Record> records = parser.parseDataset(dataset, true); // set latest=true by default
										
					// top-level dataset
					final Record drecord = records.get(0);	
					
					// publish
					if (publish) {
					    
					    // check versus existing records in the metadata repository
					    if (searchService!=null) {
					        final List<Record> exRecords = this.getLatestDatasets(drecord.getMasterId());
					        // loop over existing records
					        for (final Record exRecord : exRecords) {
					            
					            if (exRecord.getVersion()<drecord.getVersion()) {
					                // case 1) publishing a newer version:
					                // republish previous version with "latest"=false
					                
					                // retrieve previous record THREDDS catalogs URI
					                String exCatalogUri = RecordHelper.selectUrlByMimeType(exRecord, QueryParameters.MIME_TYPE_THREDDS);
					                if (StringUtils.hasText(exCatalogUri)) {
					                    
					                    final InvCatalog exCatalog = parseCatalog(exCatalogUri);
					                        
				                        for (final InvDataset exDataset : exCatalog.getDatasets()) {
				                            if (LOG.isInfoEnabled()) 
				                                LOG.info("Latest version in index: catalog uri="+exCatalogUri+" record id="
				                                        +exRecord.getId()+" record master_id="+exRecord.getMasterId()+" version="+exRecord.getVersion());
				                            if (exDataset instanceof InvDatasetImpl) {
				                                // publish previous records with "latest"=false
				                                if (LOG.isInfoEnabled()) LOG.info("Republishing dataset: "+exDataset.getID()+" with latest=false");
				                                _records.addAll( parser.parseDataset(exDataset, false));
				                            }
				                        }
					                        					                    
					                }
					              
					            } else if (exRecord.getVersion()>drecord.getVersion()) {
					                // case 2) publishing an older version:
					                // change latest flag of this version before publishing it
				                    if (LOG.isInfoEnabled()) LOG.info("Index already contains newer version: "+exRecord.getVersion()
				                                                     +" - setting latest=false for this version: "+drecord.getVersion());

					                for (final Record record : records) {
					                    record.setLatest(false);
					                }
					                
					            } else {
					                // case 3) publishing the same version:
					                // nothing to do
					            }
					        }
					    }
					    
					    // publish new and older versions as a single commit
					    records.addAll(_records);
					    callback.notify(records);
    					    					
					// un-publish
					} else {
					    
					    // remove top-level dataset only, files will follow
                        if (LOG.isDebugEnabled()) LOG.debug("Removing catalog for top-level dataset="+drecord.getId());
                        callback.notify(drecord);
					    
					}
					
				}
				
			} // loop over datasets
		    			
		// invalid catalog
		} catch(IOException e) {
            // notify listener of crawling error
            if (listener!=null) listener.afterCrawlingError(catalogURI.toString());
            // throw the exception up the stack
			throw e;
		}
		
        // notify listener of successful completion
        if (listener!=null) listener.afterCrawlingSuccess(catalogURI.toString());
				
	}
	
	/**
	 * Private method to parse a THREDDS catalog referenced by a URI into an object,
	 * leveraging the underlying THREDDS java library.
	 * 
	 * @param uri
	 * @return
	 */
	private InvCatalog parseCatalog(final String uri) throws IOException {
	    
        final InvCatalogFactory factory = new InvCatalogFactory("default", true); // validate=true
        final InvCatalog catalog = factory.readXML(uri);
        final StringBuilder buff = new StringBuilder();
        
        if (catalog.check(buff)) {
            return catalog;
        } else {
            throw new IOException("Invalid THREDDS catalog at uri: "+uri+" error: "+buff.toString());
        }
	    
	}
	
	/**
	 * Utility method to retrieve the latest version of a dataset (by master_id)
	 * from the (local) metadata index.
	 * 
	 * @param master_id
	 */
	private List<Record> getLatestDatasets(final String master_id) throws Exception {
	    
	    // query for latest records of type Dataset, by master_id, on local index only
        final SearchInput input = new SearchInputImpl(QueryParameters.DEFAULT_TYPE);
        input.setConstraint(QueryParameters.FIELD_MASTER_ID, master_id);
        input.setConstraint(QueryParameters.FIELD_LATEST, "true");
        input.setDistrib(false);
                
        // execute query
        final SearchOutput output = searchService.search(input);
        
        return output.getResults();
	    
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

    @Override
    public void setListener(MetadataRepositoryCrawlerListener listener) {
        this.listener = listener;
    }
	
}
