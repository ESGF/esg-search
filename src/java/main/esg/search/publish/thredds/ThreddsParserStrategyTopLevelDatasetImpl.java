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

import thredds.catalog.InvAccess;
import thredds.catalog.InvDataset;
import thredds.catalog.InvDocumentation;
import thredds.catalog.InvProperty;
import thredds.catalog.ThreddsMetadata.GeospatialCoverage;
import thredds.catalog.ThreddsMetadata.Variable;
import thredds.catalog.ThreddsMetadata.Variables;
import ucar.nc2.units.DateRange;
import esg.search.core.Record;
import esg.search.core.RecordHelper;
import esg.search.core.RecordImpl;
import esg.search.publish.impl.PublishingServiceMain;
import esg.search.publish.plugins.MetadataEnhancer;
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
	 * Optional map of metadata enhancers.
	 * For performance, each metadata enhancer is triggered by a single field, the map key.
	 */
	private Map<String, MetadataEnhancer> metadataEnhancers = new LinkedHashMap<String, MetadataEnhancer>();
		
	public ThreddsParserStrategyTopLevelDatasetImpl() {}
	
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
	public List<Record> parseDataset(final InvDataset dataset, final boolean latest) {
		
	    // instantiate overall list of records from this catalogs
        final List<Record> records = new ArrayList<Record>();
        
        // catalog-scope variables
        final String hostName = getHostName(dataset);

        // parse top-level dataset
        final Record record = this.parseCollection(dataset, latest, hostName);
                
        // IMPORTANT: add top-level dataset as first record
        records.add(record);
		
		// set replica flag from top-level dataset
        boolean isReplica = record.isReplica();
        // recursion
		long size = parseSubDatasets(dataset, latest, records, isReplica, hostName);
		// set total size of dataset
		record.addField(SolrXmlPars.FIELD_SIZE, Long.toString(size));
		
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
	private long parseSubDatasets(final InvDataset dataset, final boolean latest, final List<Record> records, boolean isReplica, String hostName) {
	    
	    if (LOG.isTraceEnabled()) LOG.trace("Crawling dataset: "+dataset.getID()+" for files");
	    
	    long dataset_size = 0L;
	    for (final InvDataset childDataset : dataset.getDatasets()) {
	        
	        if (StringUtils.hasText( childDataset.findProperty(ThreddsPars.FILE_ID) )) {
	            
	            // parse files into separate records, inherit top dataset metadata
	            boolean inherit = true;
	            dataset_size += this.parseFile(childDataset, latest, records, inherit, isReplica, hostName);

	        } else if (StringUtils.hasText( childDataset.findProperty(ThreddsPars.AGGREGATION_ID) )) {
	            
	            // parse aggregation INTO TOP LEVEL DATASET
	            //this.parseAggregation(childDataset, records.get(0) );
	            
	        }
	        
	        // recursion
	        dataset_size += parseSubDatasets(childDataset, latest, records, isReplica, hostName);
	        
	    }
	    
	    return dataset_size;
        
	}
	
	/**
	 * Method to parse the aggregation information into given record ( = top-level dataset)
	 * @param dataset
	 * @param record
	 */
	private void parseAggregation(final InvDataset dataset, final Record record) {
	    
	    this.parseAccess(dataset, record);
	    
	}
	
	private Record parseCollection(final InvDataset dataset, final boolean latest, final String hostName) {
	    
	    if (LOG.isDebugEnabled()) LOG.debug("Parsing dataset: "+dataset.getID());
	        	    	    
	    // create new record with universally unique record identifier
	    final Record record = newRecord(dataset, latest, hostName);
	    
	    final String name = dataset.getName();
	    Assert.notNull(name, "Dataset name cannot be null");
	    record.addField(QueryParameters.FIELD_TITLE, name);
	        
	    // type
	    record.setType(QueryParameters.TYPE_DATASET);
	    //record.addField(QueryParameters.FIELD_TYPE, SolrXmlPars.TYPE_DATASET);
	        	        
	    // encode dataset catalog as first access URL
	    final String url = dataset.getCatalogUrl();
	    record.addField(QueryParameters.FIELD_URL, 
	                    RecordHelper.encodeUrlTuple(url, 
	                                                ThreddsPars.getMimeType(url, ThreddsPars.SERVICE_TYPE_CATALOG),
	                                                ThreddsPars.SERVICE_TYPE_CATALOG));
	        
        // add indexing host name
        //final MetadataEnhancer me = metadataEnhancers.get(ThreddsPars.ID);
        //if (me!=null) me.enhance(QueryParameters.FIELD_INDEX_PEER, null, record);
                
        // FIXME
        // metadata format
        record.addField(SolrXmlPars.FIELD_METADATA_FORMAT, "THREDDS");      
        // metadata file name
        record.addField(SolrXmlPars.FIELD_METADATA_URL, PublishingServiceMain.METADATA_URL);
        
        this.parseDocumentation(dataset, record);
        
        this.parseVariables(dataset, record);
        
        this.parseAccess(dataset, record);
        
        this.parseProperties(dataset, record);

        this.parseMetadataGroup(dataset,record);
        
        this.enhanceMetadata(record);
                
        return record;
	        
	}
	
	/**
	 * Specific method to parse file-level information into a newly separate record, 
	 * which is added to the list of already existing records.
	 * 
	 * @param dataset
	 * @param records
	 * @param inherit : set to true to copy dataset fields as file record fields (without overriding existing fields)
	 * @return : the file size for computational purposes, or 0 if un-available.
	 */
	private long parseFile(final InvDataset file, final boolean latest, final List<Record> records, boolean inherit,
	                       boolean isReplica, final String hostName) {
	    
        // create new record with universally unique record identifier
        final Record record = newRecord(file, latest, hostName);
        
        // name -> title
        final String name = file.getName();
        Assert.notNull(name, "File name cannot be null");
        record.addField(QueryParameters.FIELD_TITLE, name);
        // type
        //record.addField(QueryParameters.FIELD_TYPE, SolrXmlPars.TYPE_FILE);  
        record.setType(QueryParameters.TYPE_FILE);
        // parent dataset
        record.addField(QueryParameters.FIELD_DATASET_ID, records.get(0).getId());

        long size = 0; // 0 file size by default
        this.parseProperties(file, record);
        // set size if found
        if (StringUtils.hasText( record.getFieldValue(SolrXmlPars.FIELD_SIZE)) ) {
            size = Long.parseLong(record.getFieldValue(SolrXmlPars.FIELD_SIZE));
        }
        
        this.parseVariables(file, record);
        
        this.parseAccess(file, record);
        
        this.parseDocumentation(file, record);
        
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
	    return size;
	    
	}
	
	/**
	 * Method to parse all (key,value) pair properties of a dataset into a search record fields.
	 * @param dataset
	 * @param record
	 */
	private void parseProperties(final InvDataset dataset, final Record record) {
	    
	    // <property name="..." value="..." />
        for (final InvProperty property : dataset.getProperties()) {
            
            if (LOG.isTraceEnabled()) LOG.trace("Property: " + property.getName() + "=" + property.getValue());
            
            if (property.getName().equals(ThreddsPars.DATASET_ID) || property.getName().equals(ThreddsPars.FILE_ID)) {
                // note: override "master_id" with version-independent identifier
                // <property name="dataset_id" value="obs4MIPs.NASA-JPL.AIRS.mon"/>
                // <property name="file_id" value="obs4MIPs.NASA-JPL.AIRS.mon.husNobs_AIRS_L3_RetStd-v5_200209-201105.nc"/>
                record.setMasterId(property.getValue());
                
            } else if (property.getName().equals(QueryParameters.FIELD_TITLE)) {
                // note: record title already set from dataset name
                record.addField(QueryParameters.FIELD_DESCRIPTION, property.getValue());
                
            } else if (property.getName().equals(ThreddsPars.DATASET_VERSION) || property.getName().equals(ThreddsPars.FILE_VERSION)) {
                // note: map "dataset_version", "file_version" to "version"
                try {
                    record.setVersion(Long.parseLong(property.getValue()));
                } catch (NumberFormatException e) {
                    // no version, defaults to 0
                }
                
            } else if (property.getName().equals(ThreddsPars.SIZE)) {
                record.addField(SolrXmlPars.FIELD_SIZE, property.getValue());
                
            // set replica flag
            } else if (property.getName().equals(ThreddsPars.IS_REPLICA)) {
                record.setReplica(Boolean.parseBoolean(property.getValue()));
                
            } else {
                // index all other properties verbatim
                record.addField(property.getName(), property.getValue());
            }
        }
	    
	}
	
	/**
	 * Method to parse the variable information associated with a dataset into the metadata search record
	 * @param dataset
	 * @param record
	 */
	private void parseVariables(final InvDataset dataset, final Record record) {
	    	    
	    // <variables vocabulary="CF-1.0">
        //   <variable name="hfss" vocabulary_name="surface_upward_sensible_heat_flux" units="W m-2">Surface Sensible Heat Flux</variable>
        // </variables>
        for (final Variables variables : dataset.getVariables()) {
            final String vocabulary = variables.getVocabulary();
            for (final Variable variable : variables.getVariableList()) {
                record.addField(SolrXmlPars.FIELD_VARIABLE, variable.getName());
                if (vocabulary.equals(ThreddsPars.CF)) {
                    // convert all CF names to lower case, and join by "_"
                    record.addField(SolrXmlPars.FIELD_CF_STANDARD_NAME, 
                                   variable.getVocabularyName().toLowerCase().replaceAll("\\s+", "_"));
                    // do not include if containing upper case letters or spaces
                    //final Matcher matcher = NON_CF_PATTERN.matcher(variable.getVocabularyName());
                    //if (!matcher.matches()) record.addField(SolrXmlPars.FIELD_CF_STANDARD_NAME, variable.getVocabularyName());
                }
                if (StringUtils.hasText(variable.getDescription())) record.addField(SolrXmlPars.FIELD_VARIABLE_LONG_NAME, variable.getDescription());
            }
        }
	    
	}
	
	/**
	 * Method to parse the access information associated with a dataset into the metadata record search.
     *
	 * @param dataset
	 * @param record
	 */
	private void parseAccess(final InvDataset dataset, final Record record) {
	    
	    // <access urlPath="/ipcc/sresb1/atm/3h/hfss/miroc3_2_hires/run1/hfss_A3_2050.nc" serviceName="GRIDFTPatPCMDI" dataFormat="NetCDF" />
        for (final InvAccess access : dataset.getAccess()) {
            
            if (LOG.isTraceEnabled()) LOG.trace("Dataset="+dataset.getID()+" Service="+access.getService().getName()+" URL="+access.getStandardUri().toString());
                       
            String url = access.getStandardUri().toString();
            final String type = access.getService().getServiceType().toString();
            // special processing of opendap endpoints since URL in thredds catalog is unusable without a suffix
            if (type.equalsIgnoreCase(ThreddsPars.SERVICE_TYPE_OPENDAP)) url += ".html";
            
            // encode URL tuple
            record.addField(QueryParameters.FIELD_URL, 
                            RecordHelper.encodeUrlTuple(url, ThreddsPars.getMimeType(url, type), access.getService().getDescription() ));

        }
	    
	}
	
	
	/**
	 * Method to extract metadata information from a thredds dataset
	 * Included in this metadata are the geospatial and temporal info contained
	 * in the xml tags:
	 * <>
	 * 
	 */
	private void parseMetadataGroup(final InvDataset dataset, final Record record) {
	    
		this.parseGeoSpatialCoverage(dataset,record);
		this.parseTimeCoverage(dataset,record);
		
	}
	
	/**
     * Method to extract documentation information from a dataset into the search metadata record.
     */
    private void parseDocumentation(final InvDataset dataset, final Record record) {
        
        // <documentation type="...">.......</documentation>
        for (final InvDocumentation documentation : dataset.getDocumentation()) {
            // inline documentation
            final String content = documentation.getInlineContent();
            if (StringUtils.hasText(content)) {
                record.addField(QueryParameters.FIELD_DESCRIPTION, content);
            }
            // xlink documentation
            final String href = documentation.getXlinkHref();
            if (StringUtils.hasText(href)) {
                record.addField(QueryParameters.FIELD_XLINK, RecordHelper.encodeXlinkTuple(href, documentation.getXlinkTitle(), documentation.getType()) );
            }
        }
        
    }
	
	/**
	 * Helper method to extract Geospatial metadata from a thredds dataset
	 * <geospatialCoverage zpositive="down">
	 *		<northsouth>
	 *			<start>36.6058</start>
	 *			<size>0.0</size>
	 *			<units>degrees_north</units>
	 *		</northsouth>
	 *		<eastwest>
	 *			<start>-97.4888</start>
	 *			<size>0.0</size>
	 *			<units>degrees_west</units>
	 *		</eastwest>
	 *		<updown>
	 *			<start>314.0</start>
	 *			<size>0.0</size>
	 *			<units>m</units>
	 *		</updown>
	 *	</geospatialCoverage>
	 * 
	 */
	private void parseGeoSpatialCoverage(final InvDataset dataset, final Record record) {
	    
		final GeospatialCoverage gsc = dataset.getGeospatialCoverage();
		
		if (gsc!=null) {
		    
			record.addField(SolrXmlPars.FIELD_SOUTH, Double.toString(gsc.getNorthSouthRange().getStart()));
		
			record.addField(SolrXmlPars.FIELD_NORTH, Double.toString(gsc.getNorthSouthRange().getStart()+gsc.getNorthSouthRange().getSize()));
			
			record.addField(SolrXmlPars.FIELD_WEST, Double.toString(gsc.getEastWestRange().getStart()));
			
			record.addField(SolrXmlPars.FIELD_EAST, Double.toString(gsc.getEastWestRange().getStart()+gsc.getEastWestRange().getSize()));
			
		}
	}
	
	
	/**
	 * Helper method to extract temporal metadata from a thredds dataset
	 * Note: not all representations are covered, just the following
	 * <timeCoverage zpositive="down">
	 *		<start>1999-11-16T12:00</start>
	 *		<end>2009-11-16T12:00</end>
	 *	</timeCoverage>
	 * 
	 */
	private void parseTimeCoverage(final InvDataset dataset,Record record) {
	    
		final DateRange daterange = dataset.getTimeCoverage();
		
		if (daterange!=null) {
			record.addField(SolrXmlPars.FIELD_DATETIME_START, daterange.getStart().toDateTimeStringISO());	
			record.addField(SolrXmlPars.FIELD_DATETIME_STOP, daterange.getEnd().toDateTimeStringISO());
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
	 * 
	 * @param dataset
	 * @param latest
	 * @param hostName
	 */
	private Record newRecord(final InvDataset dataset, final boolean latest, final String hostName) {
	    
	    // retrieve dataset ID from THREDDS catalog...
	    // <dataset name="...." ID="..." restrictAccess="...">
        String id = dataset.getID();
        
        // ...or assign random UUID if dataset id was not found
        if (id==null) id = UUID.randomUUID().toString();
        
        // combine dataset id with host name
        final String rid = id + ":" + hostName;
        
        final Record record = new RecordImpl(rid);
        
        // assign a default "master_id" equal to the THREDDS ID (or the UUID if not found)
        record.setMasterId(id);
        
        // set "replica"=false by default
        record.setReplica(false);
        
        // set "latest" flag as requested
        record.setLatest(latest);
        
        return record;

	}
	
	/**
	 * Method to encode master/replica information.
	 */
	/*
	private void setReplicaFields(final Record record, final boolean isReplica, final String hostName) {
	    
        if (isReplica) {
            record.addField(QueryParameters.FIELD_REPLICA, "true");
            record.addField(QueryParameters.FIELD_MASTER_ID, record.getId());
            record.setId(this.buildReplicaId(record.getId(), hostName));
        } else {
            record.addField(QueryParameters.FIELD_REPLICA, "false");
            record.addField(QueryParameters.FIELD_MASTER_ID, record.getId());
        }

	} */
	
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
