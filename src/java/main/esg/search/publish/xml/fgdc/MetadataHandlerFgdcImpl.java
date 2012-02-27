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
package esg.search.publish.xml.fgdc;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.stereotype.Component;

import esg.search.core.Record;
import esg.search.core.RecordImpl;
import esg.search.publish.impl.PublishingServiceMain;
import esg.search.publish.xml.MetadataHandler;
import esg.search.query.api.QueryParameters;
import esg.search.query.impl.solr.SolrXmlPars;

/**
 * Implementation of {@link MetadataHandler} for FGDC XML documents.
 * This class extracts information from the FGDC XML metadata
 * and transfers it to a Record object(s).
 */
@Component
public class MetadataHandlerFgdcImpl implements MetadataHandler {

	private final Log LOG = LogFactory.getLog(this.getClass());
	
	/**
	 * {@inheritDoc}
	 */
	public List<Record> parse(final Element root) {
		
		
		
		final Record record = new RecordImpl();
		final Namespace ns = root.getNamespace();
		
		//metadata format
		record.addField(QueryParameters.FIELD_METADATA_FORMAT, "FGDC");
		
		//metadata file name
		record.addField(QueryParameters.FIELD_METADATA_URL, PublishingServiceMain.METADATA_URL);
		
		
		//add the required field type
		record.addField(QueryParameters.FIELD_TYPE, "dataset");
		
		
		//<citation>
		//   <citeinfo>
		//    <origin>Shaocheng Xie, xie2@llnl.gov</origin>  
		//    <origin>Renata McCoy</origin> 
		//    <title>Atmospheric Radiation Measurement (ARM) Climate Modeling Best Estimate Product</title>
		//    <pubdate>20090812</pubdate>
		//    <onlink>http://iop.archive.arm.gov/arm-iop/0showcase-data/cmbe/</onlink>
		//    <onlink>http://www.arm.gov/data/pi/36</onlink>
		//    <onlink>http://www.arm.gov</onlink>
		//    <onlink>http://www.arm.gov/instruments</onlink>
		//   </citeinfo>
		//</citation>
		
		//<idinfo>
		final Element idinfoEl = root.getChild("idinfo");
		
		System.out.println(idinfoEl);
		
		//  <citation>
		final Element citationEl = idinfoEl.getChild("citation");
		
		//    <citeinfo>
		final Element citeinfoEl = citationEl.getChild("citeinfo");
		
		
		System.out.println(citeinfoEl);
		
		//      <origin>
		for (final Object originEl : citeinfoEl.getChildren("origin")) {
			System.out.println(originEl);
		}
		
		//	    <title>Atmospheric Radiation Measurement (ARM) Climate Modeling Best Estimate Product</title>
		final Element titleEl = citeinfoEl.getChild("title");
		//LOG.debug("adding title: " + titleEl.getText());
		final String title = titleEl.getTextNormalize();
		record.addField(QueryParameters.FIELD_TITLE, title);
		
		
		//	    <pubdate>20090812</pubdate>
		final Element pubdateEl = citeinfoEl.getChild("pubdate");
		
		
		/* This is VERY arbitrary... it should probably be considered more carefully */
		int count = 0;
		for (final Object onlinkEl : citeinfoEl.getChildren("onlink")) {
			final String onlinkStr = ((Element)onlinkEl).getTextNormalize();
		
			//<onlink>http://iop.archive.arm.gov/arm-iop/0showcase-data/cmbe/</onlink>
			if(count == 0)
			{
				record.addField(QueryParameters.FIELD_URL, onlinkStr);
			}
			//<onlink>http://www.arm.gov/data/pi/36</onlink>
			else if(count == 1)
			{
				record.setId(onlinkStr);
			}
			count++;
			
			//	  <onlink>http://www.arm.gov</onlink>
			//    <onlink>http://www.arm.gov/instruments</onlink>
			
		}
		
		
		
		/*
		<descript>
		   <abstract>
		The ARM Climate Modeling Best Estimate (CMBE) product is a new ARM datastream specifically tailored to climate modelers for use in evaluation of global climate models. It contains a best estimate of several selected cloud, radiation and atmospheric quantities from the ACRF observations and Numerical Weather Prediction (NWP) analysis (for upper air data only).
		The data are stored in two different data file streams: CMBE-CLDRAD (former CMBE datastream) for cloud and radiation relevant quantities and CMBE-ATM for atmospheric quantities. Data are averaged over one hour time intervals. Quick look plots and detailed information about the CMBE data can be found at Climate Modeling Working Group web page: http://science.arm.gov/wg/cpm/scm/best_estimate.html.
		The CMBE-CLDRAD data are available for the 5 ARM Climate Research Facility sites: SGP.C1 (Lamont, OK), NSA.C1 (Barrow, AK), TWP.C1 (Manus Island, PNG), TWP.C2 (Nauru), and TWP.C3 (Darwin, AU) for the period when these data are available. This data file contains a best estimate of several selected cloud and radiation relevant quantities from ACRF observations:
		* Cloud fraction profiles
		* Total, high, middle, and low clouds
		* Liquid water path and precipitable water vapor
		* Surface radiative fluxes
		* TOA radiative fluxes
		The CMBE-ATM data are currently only available for SGP.C1. This data file contains a best estimate of several selected atmospheric quantities from ACRF observations and NWP analysis data:
		* Soundings
		* NWP analysis data
		* Surface sensible and latent heat fluxes
		* Surface precipitation
		* Surface temperature, relative humidity, and horizontal winds</abstract>
		   <supplinf> http://science.arm.gov/workinggroup/cpm/scm/best_estimate.html</supplinf>
		   <purpose>The CMBE product was developed to make ARM data better serve the needs of climmate studies and model development. It is intended for use by climate modelers in evaluating global climate models. </purpose>
		  </descript>
		*/
		final Element descriptEl = idinfoEl.getChild("descript");
		
		//   <abstract>
		final Element abstractEl = descriptEl.getChild("abstract");
		//LOG.debug("adding description: " + abstractEl.getText());
		record.addField(QueryParameters.FIELD_DESCRIPTION, abstractEl.getTextNormalize());
		
		//   <supplinf>
		final Element supplinfEl = descriptEl.getChild("supplinf");
		
		/*
		<timeperd>
		   <timeinfo>
		    <rngdates>
		     <begdate>19960101</begdate>
		     <enddate>. </enddate>
		    </rngdates>
		   </timeinfo>
		  </timeperd>
		*/
		final Element timeperdEl = idinfoEl.getChild("timeperd");
		final Element timeinfoEl = timeperdEl.getChild("timeinfo");
		final Element rngdatesEl = timeinfoEl.getChild("rngdates");
		final Element begdateEl = rngdatesEl.getChild("begdate");
		final Element enddateEl = rngdatesEl.getChild("enddate");
		
		
		if(begdateEl != null && enddateEl != null)
		{
			String date = dateConversion(begdateEl.getTextNormalize());
			record.addField(QueryParameters.FIELD_DATETIME_START, date);
			
			date = dateConversion(enddateEl.getTextNormalize());
			record.addField(QueryParameters.FIELD_DATETIME_STOP, date);
			
			
		}
		
		/*
		<status>
		   <progress>Ongoing </progress>
		   <update>Annual </update>
		   <Maintenance_and_Update_Frequency>hourly</Maintenance_and_Update_Frequency>
		</status>
		*/
		final Element statusEl = idinfoEl.getChild("status");
		final Element progressEl = statusEl.getChild("progress");
		final Element updateEl = statusEl.getChild("update");
		
		final Element mAndUFreqEl = statusEl.getChild("Maintenance_and_Update_Frequency");
		if(mAndUFreqEl != null)
			record.addField("frequency", mAndUFreqEl.getTextNormalize());
		
		
		/*
		<spdom>
		   <bounding>
		    <westbc>-99.31</westbc>
		    <eastbc>-95.59</eastbc>
		    <northbc>38.3</northbc>
		    <southbc>34.98</southbc>
		   </bounding>
		</spdom>
		*/
		final Element spdomEl = idinfoEl.getChild("spdom");
		final Element boundingEl = spdomEl.getChild("bounding");
		
		final Element westbcEl = boundingEl.getChild("westbc");
		final Element eastbcEl = boundingEl.getChild("eastbc");
		final Element northbcEl = boundingEl.getChild("northbc");
		final Element southbcEl = boundingEl.getChild("southbc");
		
		if(westbcEl != null && eastbcEl != null && northbcEl != null && southbcEl != null)
		{
			record.addField(QueryParameters.FIELD_WEST, westbcEl.getTextNormalize());
			record.addField(QueryParameters.FIELD_EAST, eastbcEl.getTextNormalize());
			record.addField(QueryParameters.FIELD_NORTH, northbcEl.getTextNormalize());
			record.addField(QueryParameters.FIELD_SOUTH, southbcEl.getTextNormalize());
		}
		
		
		final Element keywordsEl = idinfoEl.getChild("keywords");
		
		for(final Object themeEl : keywordsEl.getChildren("theme")) {
			
			final Element themektEl = ((Element)themeEl).getChild("themekt");
			
			String themektStr = themektEl.getTextNormalize();
			
			
			//themes -- realm
			/*
			<theme>
		    	<themekt>Realm</themekt>
		    	<themekey>Land</themekey>
		    </theme> 
			*/
			if(themektStr.equals("Realm"))
			{
				final Element themekeyEl = ((Element)themeEl).getChild("themekey");
				
			}
			/*
			<theme>
		    <themekt>Instrument Categories</themekt>
		    <themekey>Cloud properties</themekey>
		    <themekey>Atmospheric profiling</themekey>
		    <themekey>Radiometric</themekey>
		   </theme>
		   */ 
			else if(themektStr.equals("Instruments"))
			{
				for(final Object themekeyEl : ((Element)themeEl).getChildren("theme")) 
				{
					String themekeyStr = ((Element)themekeyEl).getTextNormalize();
					record.addField(QueryParameters.FIELD_INSTRUMENT, themekeyStr);
				}
			}
			/*
			 <theme>
			    <themekt>Variable Names</themekt>
			    <themekey>cld_frac</themekey>
			    <themekey>cld_frac_MMCR</themekey>
			*/   
			else if(themektStr.equals("Variable Names"))
			{
				for(final Object themekeyEl : ((Element)themeEl).getChildren("theme")) 
				{
					String themekeyStr = ((Element)themekeyEl).getTextNormalize();
					record.addField(QueryParameters.FIELD_VARIABLE, themekeyStr);
				}
			}
			
			//add more themekt's here
			
			
		}
		
		final List<Record> records = new ArrayList<Record>();
		records.add(record);
		return records;
	}
	

	/**
	 * 
	 * Static helper method to convert the fgdc date format into the solr date format
	 * YYYYMMDD -> YYYY-MM-DDTHH:MM:SSZ
	 * @param oldDateString
	 * @return dateString
	 * */
	private static String dateConversion(String oldDateString)
	{
		String dateString = "";
		
		if(oldDateString.equals("."))
		{
			dateString += "NOW";
		}
		else
		{
			/* Date type yyyymmdd */
			String year = oldDateString.substring(0, 4);
			String month = oldDateString.substring(4, 6);
			String day = oldDateString.substring(6, oldDateString.length());
		
			dateString += year + "-" + month + "-" + day + "T00:00:00Z";
		}
		
		return dateString;
	}
	
	
	
}
