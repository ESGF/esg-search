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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import thredds.catalog.InvCatalogRef;
import thredds.catalog.InvDataset;
import thredds.catalog.InvDatasetImpl;
import esg.search.core.Record;
import esg.search.core.RecordHelper;
import esg.search.core.RecordImpl;
import esg.search.publish.impl.PublishingServiceMain;
import esg.search.publish.plugins.MetadataEnhancer;
import esg.search.publish.thredds.parsers.AccessParser;
import esg.search.publish.thredds.parsers.DatasetSummary;
import esg.search.publish.thredds.parsers.DocumentationParser;
import esg.search.publish.thredds.parsers.MetadataGroupParser;
import esg.search.publish.thredds.parsers.PropertiesParser;
import esg.search.publish.thredds.parsers.ThreddsElementParser;
import esg.search.publish.thredds.parsers.VariablesParser;
import esg.search.query.api.QueryParameters;
import esg.search.query.impl.solr.SolrXmlPars;

/**
 * Implementation of {@link ThreddsParserStrategy} that produces a single {@link Record} of type "Dataset" for each top-level THREDDS dataset,
 * and one {@link Record} of type "File" for each file nested anywhere in the hierarchy.
 */
@Component
public class ThreddsParserStrategyTopLevelDatasetImpl implements ThreddsParserStrategy {
	
	private final Log LOG = LogFactory.getLog(this.getClass());
	
	// anti-pattern for CF standard names, which do not contain upper case letters or spaces
    private final Pattern NON_CF_PATTERN = Pattern.compile(".*[A-Z\\s].*");
	
	/**
	 * Default URL builder
	 */
	private ThreddsDataseUrlBuilder urlBuilder = new ThreddsDatasetUrlBuilderCatalogUrlImpl();
	
	/**
	 * List of THREDDS element parsers (currently the same for each record type).
	 */
	private List<ThreddsElementParser> parsers;
	
	/**
	 * Optional map of metadata enhancers.
	 * For performance, each metadata enhancer is triggered by a single field, the map key.
	 */
	private Map<String, MetadataEnhancer> metadataEnhancers = new LinkedHashMap<String, MetadataEnhancer>();
		
	public ThreddsParserStrategyTopLevelDatasetImpl() {
	    
	    // instantiate THREDDS element parsers
	    parsers = new ArrayList<ThreddsElementParser>();
	    parsers.add( new AccessParser() );	    
	    parsers.add( new DocumentationParser() );
	    parsers.add( new MetadataGroupParser() );
	    parsers.add( new PropertiesParser() );
	    parsers.add( new VariablesParser() );
	    	    	    
	}
	
	/**
	 * Method to set the builder for the URL to be associated with each record
	 * (overriding the default strategy).
	 * 
	 * @param urlBuilder
	 */
	@Autowired
	public void setUrlBuilder(final @Qualifier("threddsDatasetUrlBuilderCatalogViewImpl") ThreddsDataseUrlBuilder urlBuilder) {
		this.urlBuilder = urlBuilder;
	}
	
	/**
	 * Method to set a map of {@link MetadataEnhancer}, keyed by the property name.
	 * Note that this map is automatically populated by Spring with all the {@link MetadataEnhancer}s beans found in the configuration,
	 * keyed by the bean id.
	 * 
	 * @param metadataEnhancers
	 */
	@Autowired
	public void setMetadataEnhancers(final Map<String, MetadataEnhancer> metadataEnhancers) {
        this.metadataEnhancers = metadataEnhancers;
    }

    /**
	 * Method to parse the catalog top-level dataset.
	 */
	@Override
	public List<Record> parseDataset(final InvDataset dataset, final boolean latest, List<URI> catalogRefs) {
		
	    // instantiate overall list of records from this catalogs
        final List<Record> records = new ArrayList<Record>();
        
        // summary metadata for this dataset
        final DatasetSummary ds = new DatasetSummary();
        
        // catalog-scope variables
        final String hostName = getHostName(dataset);

        // parse top-level dataset
        final Record record = this.parseCollection(dataset, latest, hostName, ds);
                
        // IMPORTANT: add top-level dataset as first record
        records.add(record);
		
		// set replica flag from top-level dataset
        boolean isReplica = record.isReplica();
        
        // recursion within this catalog
		parseSubDatasets(dataset, latest, isReplica, records, hostName, ds, catalogRefs);
		
		// set total size of dataset, number of files, number of aggregations
		record.addField(QueryParameters.FIELD_SIZE, Long.toString(ds.size));
		record.addField(QueryParameters.FIELD_NUMBER_OF_FILES, Long.toString(ds.numberOfFiles));
		record.addField(QueryParameters.FIELD_NUMBER_OF_AGGREGATIONS, Long.toString(ds.numberOfAggregations));
		
		// set geospatial and temporal coverage
		if (ds.dateRange!=null) {
		    if (record.getFieldValue(SolrXmlPars.FIELD_DATETIME_START)==null)
		        record.addField(SolrXmlPars.FIELD_DATETIME_START, ds.dateRange.getStart().toDateTimeStringISO());  
	        if (record.getFieldValue(SolrXmlPars.FIELD_DATETIME_STOP)==null)
	            record.addField(SolrXmlPars.FIELD_DATETIME_STOP, ds.dateRange.getEnd().toDateTimeStringISO());  
		}
		if (record.getFieldValue(SolrXmlPars.FIELD_NORTH)==null && ds.latNorth!=Double.MIN_VALUE)
		    record.addField(SolrXmlPars.FIELD_NORTH, Double.toString(ds.latNorth) );
	    if (record.getFieldValue(SolrXmlPars.FIELD_SOUTH)==null && ds.latSouth!=Double.MAX_VALUE)
	        record.addField(SolrXmlPars.FIELD_SOUTH, Double.toString(ds.latSouth) );
	    if (record.getFieldValue(SolrXmlPars.FIELD_EAST)==null && ds.lonEast!=Double.MIN_VALUE)
	        record.addField(SolrXmlPars.FIELD_EAST, Double.toString(ds.lonEast) );
	    if (record.getFieldValue(SolrXmlPars.FIELD_WEST)==null && ds.lonWest!=Double.MAX_VALUE)
	        record.addField(SolrXmlPars.FIELD_WEST, Double.toString(ds.lonWest) );
	    if (record.getFieldValue(SolrXmlPars.FIELD_HEIGHT_BOTTOM)==null && ds.heightBottom!=Double.MAX_VALUE)
            record.addField(SolrXmlPars.FIELD_HEIGHT_BOTTOM, Double.toString(ds.heightBottom) );
	    if (record.getFieldValue(SolrXmlPars.FIELD_HEIGHT_TOP)==null && ds.heightTop!=Double.MIN_VALUE)
            record.addField(SolrXmlPars.FIELD_HEIGHT_TOP, Double.toString(ds.heightTop) );
	    if (record.getFieldValue(SolrXmlPars.FIELD_HEIGHT_UNITS)==null && StringUtils.hasText(ds.heightUnits))
            record.addField(SolrXmlPars.FIELD_HEIGHT_UNITS, ds.heightUnits );
	    
	    // set summary access types
	    for (String accessType : ds.access) {
	        record.addField(QueryParameters.FIELD_ACCESS, accessType );
	    }

		// debug
		if (LOG.isDebugEnabled()) {
    		for (final Record rec : records) LOG.debug(rec);
	    }
		
		return records;
		
	}

	/**
	 * Method to parse the children of a given dataset and store metadata information in the shared list of records.
	 * The very first record in the list corresponds to the root of the dataset hierarchy.
	 * 
	 * @param dataset
	 * @param records
	 * @return
	 */
	private DatasetSummary parseSubDatasets(final InvDataset dataset, final boolean latest, final boolean isReplica,
	                              final List<Record> records, String hostName, final DatasetSummary ds,
	                              final List<URI> catalogRefs) {
	    	    
	    if (LOG.isDebugEnabled()) LOG.trace("Crawling dataset: "+dataset.getID()+" for files");
	    
	    for (final InvDataset childDataset : dataset.getDatasets()) {
	        
	        if (childDataset instanceof InvCatalogRef) {
	            
	            try {
	                catalogRefs.add( ThreddsUtils.getCatalogRef(childDataset) );
	                
	            } catch(Exception e) {
	                LOG.warn(e.getMessage());
	            }
	            
	        } else if (childDataset instanceof InvDatasetImpl) {
	        	        
    	        if (ThreddsUtils.isFile(childDataset)) {
    	            
    	            // parse files into separate records
    	            boolean inherit = true;
    	            this.parseSubDataset(childDataset, latest, isReplica, records, inherit, hostName, ds, QueryParameters.TYPE_FILE);
    
    	        } else if (ThreddsUtils.isAggregation(childDataset)) {
    	            
    	            // parse aggregations into separate records
    	            boolean inherit = false;
    	            this.parseSubDataset(childDataset, latest, isReplica, records, inherit, hostName, ds, QueryParameters.TYPE_AGGREGATION);
    	            
    	        }
    	        
    	        // recursion
    	        parseSubDatasets(childDataset, latest, isReplica, records, hostName, ds, catalogRefs);
    	        
	        }
	        
	    }
	    
	    return ds;
        
	}
	
	private Record parseCollection(final InvDataset dataset, final boolean latest, final String hostName, final DatasetSummary ds) {
	    
	    if (LOG.isDebugEnabled()) LOG.debug("Parsing dataset: "+dataset.getID());
	        	    	    
	    // create new record with universally unique record identifier
	    final Record record = newRecord(dataset, latest, hostName);
	    
	    final String name = dataset.getName();
	    Assert.notNull(name, "Dataset name cannot be null");
	    record.addField(QueryParameters.FIELD_TITLE, name);
	        
	    // type
	    record.setType(QueryParameters.TYPE_DATASET);
	        	        
	    // encode dataset catalog as first access URL
	    final String url = dataset.getCatalogUrl();
	    record.addField(QueryParameters.FIELD_URL, 
	                    RecordHelper.encodeUrlTuple(url, 
	                                                ThreddsUtils.getMimeType(url, ThreddsPars.SERVICE_TYPE_CATALOG),
	                                                ThreddsPars.SERVICE_TYPE_CATALOG));
	    
        // FIXME
        // metadata format
        record.addField(SolrXmlPars.FIELD_METADATA_FORMAT, "THREDDS");      
        // metadata file name
        record.addField(SolrXmlPars.FIELD_METADATA_URL, PublishingServiceMain.METADATA_URL);
	                        
	    // parse THREDDS elements
        for (final ThreddsElementParser parser : parsers) {
            parser.parse(dataset, record, ds);
        }
                
        this.enhanceMetadata(record);
                
        return record;
	        
	}
	
	/**
	 * Specific method to parse sub-dataset level information into a newly separate record, 
	 * which is added to the list of already existing records.
	 * 
	 * @param dataset
	 * @param records
	 * @param inherit : set to true to copy parent dataset fields as file record fields (without overriding existing fields)
	 */
	private void parseSubDataset(final InvDataset subDataset, 
	                             final boolean latest, final boolean isReplica, 
	                             final List<Record> records, boolean inherit,
	                             final String hostName, 
	                             final DatasetSummary ds,
	                             final String recordType) {
	    
        // create new record with universally unique record identifier
        final Record record = newRecord(subDataset, latest, hostName);
        
        // set replica flag same as top-level dataset
        record.setReplica(isReplica);
        
        // name -> title
        final String name = subDataset.getName();
        Assert.notNull(name, "File name cannot be null");
        record.addField(QueryParameters.FIELD_TITLE, name);
        
        // type
        record.setType(recordType);
        
        // parent dataset
        record.addField(QueryParameters.FIELD_DATASET_ID, records.get(0).getId());
        
        // parse THREDDS elements
        for (final ThreddsElementParser parser : parsers) {
            parser.parse(subDataset, record, ds);
        }

        // set size if found
        long size = 0; // 0 file size by default
        if (StringUtils.hasText( record.getFieldValue(SolrXmlPars.FIELD_SIZE)) ) {
            size = Long.parseLong(record.getFieldValue(SolrXmlPars.FIELD_SIZE));
        }
               
        this.enhanceMetadata(record);
                
        // copy all fields from parent dataset to file
        if (inherit) {
            final Map<String, List<String>> datasetFields = records.get(0).getFields();
            for (final String key : datasetFields.keySet()) {  
                if (!key.equals(QueryParameters.FIELD_XLINK)) { // // don't inherit documentation links
                    // don't override file-level properties
                    if (record.getFieldValue(key)==null) {
                        for (final String value : datasetFields.get(key)) {
                            record.addField(key, value);
                        }
                    }            
                }
            }
        }
        	    
        // add this record to the list
        records.add(record);
        
        // summary information
        if (recordType.equals(QueryParameters.TYPE_FILE)) {
            ds.size += size;
            ds.numberOfFiles += 1;
                
        } else if (recordType.equals(QueryParameters.TYPE_AGGREGATION)) {
            ds.numberOfAggregations += 1;
        }
        	    
	}	
	
	/**
	 * Method to extract the host name from the THREDDS catalog.
	 * @param dataset
	 */
	private String getHostName(final InvDataset dataset) {

	    String hostName = "";
	    try {
	         hostName = (new URL(dataset.getCatalogUrl())).getHost();	        
	    } catch(MalformedURLException e) {}
	    if (!StringUtils.hasText(hostName)) hostName = "localhost";
        return hostName;
	}
	
	/**
	 * Factory method to create a new record for a THREDDS dataset or file:
	 * o) the record is assigned a universally unique "id"
	 * o) the record is assigned a default "master_id" which can be overridden later from the dataset properties
	 * o) the "replica" flag is set to false by default, and can be overridden later from the dataset properties
	 * o) the "latest" flag is set as requested
	 * o) the "data_node" field is assigned the hostName value
	 * 
	 * @param dataset
	 * @param latest
	 * @param hostName
	 */
	private Record newRecord(final InvDataset dataset, final boolean latest, final String hostName) {
	    
	    // retrieve dataset ID from THREDDS catalog...
	    // <dataset name="...." ID="..." restrictAccess="...">
        //String id = dataset.getID().replaceAll("/", "."); // FIXME: replace '/' in identifiers ?
        String id = dataset.getID();
        
        // ...or assign random UUID if dataset id was not found
        if (id==null) id = UUID.randomUUID().toString();
                
        // combine dataset id with host name and version
        final String rid = RecordHelper.getUniqueRecordId(id, hostName);
        
        final Record record = new RecordImpl(rid);
        
        // assign a default "master_id" equal to the THREDDS ID (or the UUID if not found)
        // may later be overridden from property "dataset_id", "file_id" or "aggregation_id", if found
        record.setMasterId(id);
        
        // assign "instance_id" equal to the THREDDS ID (or the UUID if not found)
        record.setInstanceId(id);
        
        // set "replica"=false by default
        record.setReplica(false);
        
        // set "latest" flag as requested
        record.setLatest(latest);
        
        // "data_node" field
        record.setField(QueryParameters.FIELD_DATA_NODE, hostName);
        
        return record;

	}
	
	/**
	 * Utility method to apply the configured metadata enhancers
	 * @param record
	 */
	private void enhanceMetadata(final Record record) {
        
        final Map<String, List<String>> fields = record.getFields();
        for (final String field : new ArrayList<String>(fields.keySet())) {
            // note: retrieve bean by adopted naming convention
            String key = field + "MetadataEnhancer";
            if (metadataEnhancers.containsKey(key)) {
                final MetadataEnhancer me = metadataEnhancers.get(key);
                if (me.forType(record.getType())) {
                    me.enhance(field, record.getFieldValues(field), record);
                }
            }
        }
        
	}
	
}
