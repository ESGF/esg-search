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

import java.util.ArrayList;
import java.util.List;

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
import esg.search.publish.impl.PublishingServiceMain;
import esg.search.publish.impl.RecordHelper;
import esg.search.query.impl.solr.SolrXmlPars;

/**
 * Implementation of {@link ThreddsParserStrategy} that produces a single {@link Record} for each top-level THREDDS dataset.
 */
@Component
public class ThreddsParserStrategyTopLevelDatasetImpl implements ThreddsParserStrategy {
	
	private final Log LOG = LogFactory.getLog(this.getClass());
	
	/**
	 * Default URL builder
	 */
	private ThreddsDataseUrlBuilder urlBuilder = new ThreddsDatasetUrlBuilderCatalogUrlImpl();
		
	public ThreddsParserStrategyTopLevelDatasetImpl() {}
	
	/**
	 * Method to set the builder for the URL to be associated with each record
	 * (overriding the default strategy).
	 * 
	 * @param urlBuilder
	 */
	@Autowired
	public void setUrlBuilder(final @Qualifier("threddsDatasetUrlBuilderCatalogViewImpl") ThreddsDataseUrlBuilder urlBuilder) {
	    System.out.println("URL Builder");
		this.urlBuilder = urlBuilder;
	}
	
	public List<Record> parseDataset(final InvDataset dataset) {
		
	    LOG.debug("ParseDataset");
	    
	    
		final List<Record> records = new ArrayList<Record>();
		
		LOG.debug("Dataset: " + dataset);
        
		
		// <dataset name="...." ID="..." restrictAccess="...">
		final String id = dataset.getID();
		Assert.notNull(id,"Dataset ID cannot be null");
		final Record record = new RecordImpl(id);
		final String name = dataset.getName();
		Assert.notNull(name, "Dataset name cannot be null");
		record.addField(SolrXmlPars.FIELD_TITLE, name);
		//record.addField(SolrXmlPars.FIELD_TITLE, "hello");
        
		// catalog URL
		record.addField(SolrXmlPars.FIELD_URL, urlBuilder.buildUrl(dataset));
		
		//metadata format
		record.addField(SolrXmlPars.FIELD_METADATA_FORMAT, "THREDDS");
		
		//metadata file name
		record.addField(SolrXmlPars.FIELD_METADATA_URL, PublishingServiceMain.METADATA_URL);
		
		// type
		record.addField(SolrXmlPars.FIELD_TYPE, "Dataset");
		
		// <documentation type="...">.......</documentation>
		for (final InvDocumentation documentation : dataset.getDocumentation()) {
			final String content = documentation.getInlineContent();
			if (StringUtils.hasText(content)) {
				record.addField(SolrXmlPars.FIELD_DESCRIPTION, content);
			}
		}
		
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
		
		// <property name="..." value="..." />
		for (final InvProperty property : dataset.getProperties()) {
			if (property.getName().equals(ThreddsPars.DATASET_ID)) {
				// note: override record ID to get rid of version
				// <dataset name="TES Level 3 Monthly Data (NetCDF)" ID="nasa.jpl.tes.monthly.v1" restrictAccess="esg-user">
				// <property name="dataset_id" value="nasa.jpl.tes.monthly" />
				record.setId(property.getValue());
			} else if (property.getName().equals(SolrXmlPars.FIELD_TITLE)) {
				// note: record title already set from dataset name
				record.addField(SolrXmlPars.FIELD_DESCRIPTION, property.getValue());
			} else if (property.getName().equals(ThreddsPars.DATASET_VERSION)) {
				record.addField(SolrXmlPars.FIELD_VERSION, property.getValue());
			} else {
				record.addField(property.getName(), property.getValue());
			}
		}
		
		// <access urlPath="/ipcc/sresb1/atm/3h/hfss/miroc3_2_hires/run1/hfss_A3_2050.nc" serviceName="GRIDFTPatPCMDI" dataFormat="NetCDF" />
		for (final InvAccess access : dataset.getAccess()) {
			record.addField(SolrXmlPars.FIELD_SERVICE, 
					        RecordHelper.encodeServiceField(access.getService().getServiceType().toString(), 
					        		                        access.getService().getDescription(),
					        		                        access.getStandardUrlName()));
		}

		
		// helper method for obtaining temporal and spatial metadata (as well as other) info... 
		//NOTE: might need to change signature for clarification purposes
		//		this is just a temp fix to get the geospatial data extracted
		addThreddsMetadataGroup(dataset,record);
		
		
		// get subdataset names and urls here
		crawlTopLevelDataset(dataset,record);
		
		
		LOG.debug("Record: " + record);
		
		records.add(record);
		return records;
		
	}

	private void crawlTopLevelDataset(final InvDataset dataset,Record record)
	{
	    LOG.debug("\tIn Top Level Dataset");
	    
	    ArrayList<InvDataset> datasets = (ArrayList<InvDataset>) dataset.getDatasets();
	    
	    for(final InvDataset childDataset : dataset.getDatasets())
	    {
	        String serviceType = childDataset.getAccess().get(0).getService().getName();
	        if(serviceType.equals("HTTPServer")) {
	            //LOG.debug("\t\tUrl: " + childDataset.getAccess().get(0).getStandardUri());
	            record.addField(SolrXmlPars.FIELD_CHILD_DATASET_ID, childDataset.getID());
	            record.addField(SolrXmlPars.FIELD_CHILD_DATASET_URL, childDataset.getAccess().get(0).getStandardUri().toString());
	        }
	        
	    }
        
	}
	
	
	/**
	 * Method to extract metadata information from a thredds dataset
	 * Included in this metadata are the geospatial and temporal info contained
	 * in the xml tags:
	 * <>
	 * 
	 */
	private void addThreddsMetadataGroup(final InvDataset dataset,Record record)
	{
		this.addGeoSpatialCoverage(dataset,record);
		
		this.addTimeCoverage(dataset,record);
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
	private void addGeoSpatialCoverage(final InvDataset dataset,Record record) {
		GeospatialCoverage gsc = dataset.getGeospatialCoverage();
		
		if(gsc!=null)
		{
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
	private void addTimeCoverage(final InvDataset dataset,Record record) {
		DateRange daterange = dataset.getTimeCoverage();
		
		if(daterange!=null)
		{
			record.addField(SolrXmlPars.FIELD_DATETIME_START, daterange.getStart().toDateTimeStringISO());
		
			record.addField(SolrXmlPars.FIELD_DATETIME_STOP, daterange.getEnd().toDateTimeStringISO());
		}
	}
	
}
