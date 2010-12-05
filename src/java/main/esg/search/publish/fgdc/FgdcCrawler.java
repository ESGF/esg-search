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
package esg.search.publish.fgdc;

import java.net.URI;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import esg.search.core.Record;
import esg.search.publish.api.MetadataRepositoryCrawler;
import esg.search.publish.api.MetadataRepositoryType;
import esg.search.publish.api.RecordProducer;
import esg.search.publish.xml.MetadataHandler;
import esg.search.utils.HttpClient;
import esg.search.utils.XmlParser;

/**
 * Implementation of {@link MetadataRepositoryCrawler} that acts as an OAI Harvester to retrieve records from an OAI Repository.
 */
@Service
public class FgdcCrawler implements MetadataRepositoryCrawler  {
	
	private final MetadataHandler metadataHandler;
	
	private final Log LOG = LogFactory.getLog(this.getClass());
	
	@Autowired
	public FgdcCrawler(final @Qualifier("metadataHandlerFgdcImpl") MetadataHandler metadataHandler) {
		this.metadataHandler = metadataHandler;
	}

	/**
	 * {@inheritDoc}
	 */
	public void crawl(final URI uri, final boolean recursive, final RecordProducer callback) throws Exception {
		LOG.debug("FGDCCrawler.crawl");
		
		// parse XML document
		final String xml = (new HttpClient()).doGet( uri.toURL() );
		final XmlParser xmlParser = new XmlParser(false);
		final Document doc = xmlParser.parseString(xml);
		// process XML
		this.parseDocument(doc, callback);
		
		
		
		/*
		
		*/
	}
	
	/**
	 * {@inheritDoc}
	 */
	public MetadataRepositoryType supports() {
		return MetadataRepositoryType.FGDC;
	}
	
	/**
	 * Method to parse a single FGDC document.
	 * @param doc
	 * @throws Exception
	 */
	private void parseDocument(final Document doc, final RecordProducer callback) throws Exception {
		
		LOG.debug("FGDCCrawler.parseDocument");
		
		// parse OAI response header
		
		final Element metadataEl = doc.getRootElement();
		
		// <metadata>
		
		//final Element metadataEl = root.getChild("metadata", ns);
		
		this.parseRecord(metadataEl, callback);
		
		
		/*
		// <OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/"
		//	 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		//	 xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/
		//	 http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
		//	<responseDate>2010-05-10T14:20:15Z</responseDate>
		//	<request verb="ListRecords" metadataPrefix="oai_dif">http://mercury.ornl.gov/oai/provider</request>
		
		// loop over OAI records
		// <ListRecords>
		final Element listRecordsEl = root.getChild("ListRecords", ns);
		for (final Object recordEl : listRecordsEl.getChildren("record", ns) ) {
			this.parseRecord( (Element)recordEl, callback );
		}
		*/
	}
	
	/**
	 * Method to parse a single OAI record,
	 * which contains embedded metadata of some specific format.
	 * @param recordEl
	 * @return
	 */
	private void parseRecord(final Element recordEl, final RecordProducer callback) throws Exception {
		
		LOG.debug("FGDCCrawler.parseRecord");
		
		//final Namespace ns = recordEl.getNamespace();
		
		//System.out.println(ns.getURI());
		
		System.out.println(recordEl.getChildren().get(0));
		
		final List<Record> records = metadataHandler.parse( (Element)recordEl );
		
		
		//<idinfo>
		
		/*
		if (recordEl!=null) {
			for (final Object rootEl : recordEl.getChildren()) {
				
				Element e = (Element)rootEl;
				
				System.out.println(e.getChildren());
				System.exit(0);
				// parse detailed metadata with specific handler
				final List<Record> records = metadataHandler.parse( (Element)rootEl );
				
				

				System.exit(0);
				// index resulting Solr records
				//for (final Record record : records) callback.notify(record);
			
			}
		}	
		*/
		
		/**
		 * Parse record header.
		 * <header>
      	 *   <identifier>oai:daac.ornl.gov:ornldaac_1</identifier>
         *   <datestamp>2009-07-21T22:28:58Z</datestamp>
	     * </header>
		 */
		/*
		final Namespace ns = recordEl.getNamespace();
		final Element headerEl = recordEl.getChild("header", ns);
		final String status = headerEl.getAttributeValue("status");
		final Element identifierEl = headerEl.getChild("identifier", ns);
		final String oaiIdentfier = identifierEl.getTextNormalize();
		final Element datestampEl = headerEl.getChild("datestamp", ns);
		final String datestamp = datestampEl.getTextNormalize();
		
		if (LOG.isInfoEnabled()) LOG.info("OAI Record identifier="+oaiIdentfier+" date stamp="+datestamp+" status="+status);
		
		final Element metadataEl = recordEl.getChild("metadata", ns);
		if (metadataEl!=null) {
			for (final Object rootEl : metadataEl.getChildren()) {
	
				// parse detailed metadata with specific handler
				final List<Record> records = metadataHandler.parse( (Element)rootEl );
				
				// index resulting Solr records
				for (final Record record : records) callback.notify(record);
			
			}
		}
		*/		
	}
	

}
