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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import esg.search.core.RecordImpl;
import esg.search.publish.api.MetadataEnhancer;
import esg.search.publish.impl.PublishingServiceMain;
import esg.search.publish.impl.RecordHelper;
import esg.search.query.api.QueryParameters;
import esg.search.query.impl.solr.SolrXmlPars;

/**
 * Implementation of {@link ThreddsParserStrategy} that produces a single {@link Record} of type "Dataset" for each top-level THREDDS dataset,
 * and one {@link Record} of type "File" for each file nested anywhere in the hierarchy.
 */
@Component
public class ThreddsParserStrategyTopLevelDatasetImpl implements ThreddsParserStrategy {
	
	private final Log LOG = LogFactory.getLog(this.getClass());
	
	/**
	 * Default URL builder
	 */
	private ThreddsDataseUrlBuilder urlBuilder = new ThreddsDatasetUrlBuilderCatalogUrlImpl();
	
	/**
	 * Optional map of metadata enhancers
	 */
	private Map<String, MetadataEnhancer> metadataEnhancers = new HashMap<String, MetadataEnhancer>();
		
	public ThreddsParserStrategyTopLevelDatasetImpl() {
	    
	    metadataEnhancers.put(ThreddsPars.EXPERIMENT, new ExperimentMetadataEnhancer());
	    
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
	 * @param metadataEnhancers
	 */
	public void setMetadataEnhancers(final @Qualifier("metadataEnhancers") Map<String, MetadataEnhancer> metadataEnhancers) {
        this.metadataEnhancers = metadataEnhancers;
    }

    /**
	 * Method to parse the catalog top-level dataset.
	 */
	public List<Record> parseDataset(final InvDataset dataset) {
		
	    if (LOG.isDebugEnabled()) LOG.debug("Parsing dataset: "+dataset.getID());
	    	    
		final List<Record> records = new ArrayList<Record>();
		
		// <dataset name="...." ID="..." restrictAccess="...">
		String id = dataset.getID();
		// assign random UUID if dataset id was not found
		if (id==null) id = UUID.randomUUID().toString();
		Assert.notNull(id,"Dataset ID cannot be null");
		final Record record = new RecordImpl(id);
		final String name = dataset.getName();
		Assert.notNull(name, "Dataset name cannot be null");
		record.addField(QueryParameters.FIELD_TITLE, name);
		
	    // type
		record.setType(SolrXmlPars.TYPE_DATASET);
        //record.addField(QueryParameters.FIELD_TYPE, SolrXmlPars.TYPE_DATASET);
		
		// IMPORTANT: add top-level dataset as first record in the list
		records.add(record);
        
		// encode dataset catalog as first access URL
		final String url = dataset.getCatalogUrl();
		record.addField(QueryParameters.FIELD_URL, 
		                RecordHelper.encodeUrlTuple(url, 
		                                            ThreddsPars.getMimeType(url, ThreddsPars.SERVICE_TYPE_CATALOG),
		                                            ThreddsPars.SERVICE_TYPE_CATALOG));
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
		
        // "is_replica" > id, master_id, "replica"
		// note: do this AFTER dataset ID has been overridden to get rid of version
        boolean isReplica = Boolean.valueOf(record.getFieldValue(ThreddsPars.IS_REPLICA));
        String hostName = getHostName(dataset);
		this.setReplicaFields(record, isReplica, hostName);
		
		// recursion
		// NOTE: currently only files generate new records
		long size = parseSubDatasets(dataset, records, isReplica, hostName);
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
	 * @param dataset
	 * @param records
	 * @return
	 */
	private long parseSubDatasets(final InvDataset dataset, final List<Record> records, boolean isReplica, String hostName) {
	    
	    if (LOG.isTraceEnabled()) LOG.trace("Crawling dataset: "+dataset.getID()+" for files");
	    
	    long dataset_size = 0L;
	    for (final InvDataset childDataset : dataset.getDatasets()) {
	        
	        if (StringUtils.hasText( childDataset.findProperty(ThreddsPars.FILE_ID) )) {
	            
	            // parse files into separate records, inherit top dataset metadata
	            boolean inherit = true;
	            dataset_size += this.parseFile(childDataset, records, inherit, isReplica, hostName);

	        } else if (StringUtils.hasText( childDataset.findProperty(ThreddsPars.AGGREGATION_ID) )) {
	            
	            // parse aggregation INTO TOP LEVEL DATASET
	            //this.parseAggregation(childDataset, records.get(0) );
	            
	        }
	        
	        // recursion
	        dataset_size += parseSubDatasets(childDataset, records, isReplica, hostName);
	        
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
	
	/**
	 * Specific method to parse file information (into a new separate record)
	 * @param dataset
	 * @param records
	 * @param inherit : set to true to copy dataset fields as file record fields (without overriding existing fields)
	 * @return
	 */
	private long parseFile(final InvDataset file, final List<Record> records, boolean inherit,
	                       boolean isReplica, final String hostName) {
	    
	    // <dataset name="hus_AQUA_AIRS_L3_RetStd-v5_200209-201006.nc" 
        //          ID="obs4cmip5.NASA-JPL.AQUA.AIRS.mon.v1.hus_AQUA_AIRS_L3_RetStd-v5_200209-201006.nc" 
        //          urlPath="esg_dataroot/obs4cmip5/observations/atmos/hus/mon/grid/NASA-JPL/AQUA/AIRS/r1i1p1/hus_AQUA_AIRS_L3_RetStd-v5_200209-201006.nc" 
        //          restrictAccess="esg-user">

	    final String id = file.getID();
	    Assert.notNull(id,"File ID cannot be null");
	    if (LOG.isTraceEnabled()) LOG.trace("Parsing file id="+id);
        final Record record = new RecordImpl(id);
        // name -> title
        final String name = file.getName();
        Assert.notNull(name, "File name cannot be null");
        record.addField(QueryParameters.FIELD_TITLE, name);
        // type
        //record.addField(QueryParameters.FIELD_TYPE, SolrXmlPars.TYPE_FILE);  
        record.setType(SolrXmlPars.TYPE_FILE);
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
        
        // "is_replica" > id, master_id, "replica"
        // note: do this BEFORE copying fields from Dataset!
        // note: base logic on <replica_host> property from top-level dataset
        this.setReplicaFields(record, isReplica, hostName);
        
        // copy all fields from parent dataset to file
        if (inherit) {
            final Map<String, List<String>> datasetFields = records.get(0).getFields();
            for (final String key : datasetFields.keySet()) {  
                // don't override file-level properties
                if (record.getFieldValue(key)==null) {
                    for (final String value : datasetFields.get(key)) {
                        record.addField(key, value);
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
            
            if (property.getName().equals(ThreddsPars.DATASET_ID)) {
                // note: override record ID to get rid of version
                // <dataset name="TES Level 3 Monthly Data (NetCDF)" ID="nasa.jpl.tes.monthly.v1" restrictAccess="esg-user">
                // <property name="dataset_id" value="nasa.jpl.tes.monthly" />
                record.setId(property.getValue());
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
            
            } else if (property.getName().equals(ThreddsPars.EXPERIMENT)) {
                
                // add "experiment"
                record.addField(property.getName(), property.getValue());
                
                // add "experiment_family"
                final MetadataEnhancer me = metadataEnhancers.get(ThreddsPars.EXPERIMENT);
                final Map<String, List<String>> emeta = me.enhance(property.getName(), property.getValue());
                for (final String ekey : emeta.keySet()) {
                    for (final String evalue : emeta.get(ekey)) {
                        record.addField(ekey, evalue);
                    }
                }
                
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
                if (vocabulary.equals(ThreddsPars.CF)) record.addField(SolrXmlPars.FIELD_CF_VARIABLE, variable.getDescription());
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
            final String content = documentation.getInlineContent();
            if (StringUtils.hasText(content)) {
                record.addField(QueryParameters.FIELD_DESCRIPTION, content);
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
	 * Method to build the universally unique id of a replica record
	 * by using the hostname of the THREDDS catalog.
	 * 
	 * @param id
	 * @param replicaHostName
	 * @return
	 */
	private String buildReplicaId(final String id, final String hostName) {
	    return id + ":" + hostName;
	}
	
	/**
	 * Method to encode master/replica information.
	 */
	private void setReplicaFields(final Record record, final boolean isReplica, final String hostName) {
	    
        if (isReplica) {
            record.addField(QueryParameters.FIELD_REPLICA, "true");
            record.addField(QueryParameters.FIELD_MASTER_ID, record.getId());
            record.setId(this.buildReplicaId(record.getId(), hostName));
        } else {
            record.addField(QueryParameters.FIELD_REPLICA, "false");
            record.addField(QueryParameters.FIELD_MASTER_ID, record.getId());
        }

	}
		
	
}
