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
package esg.search.publish.xml.dif;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.stereotype.Component;

import esg.search.core.Record;
import esg.search.core.RecordImpl;
import esg.search.publish.xml.MetadataHandler;
import esg.search.query.impl.solr.SolrXmlPars;

/**
 * Implementation of {@link MetadataHandler} for DIF XML documents.
 * This class extracts information from the DIF XML metadata
 * and transfers it to a Record object(s).
 */
@Component
public class MetadataHandlerDifImpl implements MetadataHandler {

	/**
	 * {@inheritDoc}
	 */
	public List<Record> parse(final Element root) {
		
		
		final Record record = new RecordImpl();
		final Namespace ns = root.getNamespace();
		
		// <Entry_ID>FIFE_TEMP_PRO</Entry_ID>
		final Element entryIdEl = root.getChild("Entry_ID", ns);
		final String entryId = entryIdEl.getTextNormalize();
		record.setId(entryId);
		
		// type
		record.addField(SolrXmlPars.FIELD_TYPE, "Dataset");
		
		// <Entry_Title>TEMPERATURE PROFILES: RADIOSONDE (FIFE)</Entry_Title>
		final Element entryTitleEl = root.getChild("Entry_Title", ns);
		final String entryTitle = entryTitleEl.getTextNormalize();
		record.addField(SolrXmlPars.FIELD_TITLE, entryTitle);
		
		// <Summary>ABSTRACT: The gravimetrical soil moisture data were collected....
		final Element summaryEl = root.getChild("Summary", ns);
		record.addField(SolrXmlPars.FIELD_DESCRIPTION, summaryEl.getTextNormalize());
		
		// </Parameters>
		for (final Object parametersEl : root.getChildren("Parameters", ns)) {
			final String parameter = parseParameter( (Element)parametersEl );
			record.addField(SolrXmlPars.FIELD_GCMD_VARIABLE, parameter);
		}
		
		// <Project>
		//   <Short_Name>EOSDIS</Short_Name>
		//   <Long_Name>Earth Observing System Data Information System</Long_Name>
		// </Project>
		for (final Object _projectEl : root.getChildren("Project", ns)) {
			final Element projectEl = (Element)_projectEl;
			final Element shortNameEl = projectEl.getChild("Short_Name", ns);
			final String project = shortNameEl.getTextNormalize();
			//final Element longNameEl = projectEl.getChild("Long_Name", ns);
			record.addField(SolrXmlPars.FIELD_PROJECT, project);
		}
		
		// <Sensor_Name>
		//   <Short_Name/>
		//   <Long_Name>RADIO ALTIMETER </Long_Name>
		// </Sensor_Name>
		for (final Object _sensorEl : root.getChildren("Sensor_Name", ns)) {
			final Element sensorEl = (Element)_sensorEl;
			final Element shortNameEl = sensorEl.getChild("Short_Name", ns);
			final Element longNameEl = sensorEl.getChild("Long_Name", ns);
			final String instrument = longNameEl.getTextNormalize();
			record.addField(SolrXmlPars.FIELD_INSTRUMENT, instrument);
		}
		
		// <Related_URL>
		//   <URL_Content_Type>
		//     <Type>GET DATA</Type>
		//     <Subtype/>
		//   </URL_Content_Type>
		//   <URL>http://daac.ornl.gov/cgi-bin/dsviewer.pl?ds_id=110</URL>
		// </Related_URL>
		for (final Object _relatedUrlEl : root.getChildren("Related_URL", ns)) {
			final Element relatedUrlEl = (Element)_relatedUrlEl;
			final Element urlEl = relatedUrlEl.getChild("URL", ns);
			final Element contentTypeEl = relatedUrlEl.getChild("URL_Content_Type", ns);
			final Element typeEl = contentTypeEl.getChild("Type", ns);
			final String type = typeEl.getTextNormalize();
			if (type.equals("GET DATA")) {
				record.addField(SolrXmlPars.FIELD_URL, urlEl.getTextNormalize());
			}
		}
		
		// <Spatial_Coverage>
		//   <Southernmost_Latitude>39.07</Southernmost_Latitude>
		//	 <Northernmost_Latitude>39.07</Northernmost_Latitude>
		//	 <Westernmost_Longitude>-96.54</Westernmost_Longitude>
		//	 <Easternmost_Longitude>-96.54</Easternmost_Longitude>
		// </Spatial_Coverage>
		for (final Object _geoEl : root.getChildren("Spatial_Coverage", ns)) {
			
			final Element _spatial_coverageEl = (Element)_geoEl;
			
			final Element _Southernmost_LatitudeEl = _spatial_coverageEl.getChild("Southernmost_Latitude", ns);
			record.addField(SolrXmlPars.FIELD_SOUTH, _Southernmost_LatitudeEl.getTextNormalize());
			
			final Element _Northernmost_LatitudeEl = _spatial_coverageEl.getChild("Northernmost_Latitude", ns);
			record.addField(SolrXmlPars.FIELD_NORTH, _Northernmost_LatitudeEl.getTextNormalize());
			
			final Element _Westernmost_LatitudeEl = _spatial_coverageEl.getChild("Westernmost_Longitude", ns);
			record.addField(SolrXmlPars.FIELD_WEST, _Westernmost_LatitudeEl.getTextNormalize());
			
			final Element _Easternmost_LatitudeEl = _spatial_coverageEl.getChild("Easternmost_Longitude", ns);
			record.addField(SolrXmlPars.FIELD_EAST, _Easternmost_LatitudeEl.getTextNormalize());
			
		}
		
		//<Temporal_Coverage>
		//<Start_Date>1987-06-24</Start_Date>
		//<Stop_Date>1987-07-11</Stop_Date>
		//</Temporal_Coverage>
		
		
		for (final Object _geoEl : root.getChildren("Temporal_Coverage", ns)) {
			final Element _spatial_coverageEl = (Element)_geoEl;
			
			final Element _Start_DateEl = _spatial_coverageEl.getChild("Start_Date", ns);
			record.addField(SolrXmlPars.FIELD_DATETIME_START, dateToISO8601(_Start_DateEl.getTextNormalize()));
			
			final Element _Stop_DateEl = _spatial_coverageEl.getChild("Stop_Date", ns);
			record.addField(SolrXmlPars.FIELD_DATETIME_STOP, dateToISO8601(_Stop_DateEl.getTextNormalize()));
			
		}
		
		
		final List<Record> records = new ArrayList<Record>();
		records.add(record);
		return records;
	}
	
	private String parseParameter(final Element parametersEl) {
		
		final StringBuilder sb = new StringBuilder();
		final Namespace ns = parametersEl.getNamespace();
		
		// <Category>EARTH SCIENCE</Category>
		final Element categoryEl = parametersEl.getChild("Category", ns);
		//sb.append(categoryEl.getTextNormalize());
		
		// <Topic>ATMOSPHERE </Topic>
		final Element topicEl = parametersEl.getChild("Topic", ns);
		sb.append(topicEl.getTextNormalize());
		
		// <Term>ATMOSPHERIC TEMPERATURE </Term>
		final Element termEl = parametersEl.getChild("Term", ns);
		sb.append(" > ").append(termEl.getTextNormalize());
		
		// <Variable_Level_1>POTENTIAL TEMPERATURE </Variable_Level_1>
		final Element variableLevel1El = parametersEl.getChild("Variable_Level_1", ns);
		if (variableLevel1El != null) {
			sb.append(" > ").append(variableLevel1El.getTextNormalize());
		}
		
		return sb.toString();
		
	}
	
	/**
     * Static method returns the input to the SearchController
     * @param parValue time in full date format
     */
    private static String dateToISO8601(String parValue)
    {
        String theDate = "";
        
        if(parValue.equals(""))
        {
            theDate = "*";
        }
        else
        {
            String [] tokens = parValue.split("-");
            String year = tokens[0] + "-";
            String month = tokens[1] + "-";
            String day = tokens[2];
            String time = "T12:00:00Z";
            theDate = year + month + day + time;
        }  
        return theDate;
    }

}
