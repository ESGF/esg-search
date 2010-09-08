package esg.search.harvest.oai;

import java.net.URI;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import esg.search.core.Record;
import esg.search.harvest.impl.MetadataHarvester;
import esg.search.harvest.xml.MetadataHandler;
import esg.search.utils.HttpClient;
import esg.search.utils.XmlParser;

/**
 * Implementation of {@link MetadataHarvester} that acts as an OAI Harvester to retrieve records from an OAI Repository.
 */
public class OaiHarvester extends MetadataHarvester  {
	
	private final MetadataHandler metadataHandler;
	
	private final Log LOG = LogFactory.getLog(this.getClass());
	
	public OaiHarvester(final MetadataHandler metadataHandler) {
		this.metadataHandler = metadataHandler;
	}

	/**
	 * {@inheritDoc}
	 */
	public void crawl(final URI uri, final boolean recursive) throws Exception {
		
		// parse XML document
		final String xml = (new HttpClient()).doGet( uri.toURL() );
		final XmlParser xmlParser = new XmlParser(false);
		final Document doc = xmlParser.parseString(xml);
		
		// process XML
		this.parseDocument(doc);
		
	}
	
	/**
	 * Method to parse a single OAI document, composed of many OAI records.
	 * @param doc
	 * @throws Exception
	 */
	private void parseDocument(final Document doc) throws Exception {
		
		
		// parse OAI response header
		// <OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/"
		//	 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		//	 xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/
		//	 http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
		//	<responseDate>2010-05-10T14:20:15Z</responseDate>
		//	<request verb="ListRecords" metadataPrefix="oai_dif">http://mercury.ornl.gov/oai/provider</request>
		final Element root = doc.getRootElement();
		final Namespace ns = root.getNamespace();
		
		// loop over OAI records
		// <ListRecords>
		final Element listRecordsEl = root.getChild("ListRecords", ns);
		for (final Object recordEl : listRecordsEl.getChildren("record", ns) ) {
			this.parseRecord( (Element)recordEl );
		}
		
	}
	
	/**
	 * Method to parse a single OAI record,
	 * which contains embedded metadata of some specific format.
	 * @param recordEl
	 * @return
	 */
	private void parseRecord(final Element recordEl) throws Exception {
		
		/**
		 * Parse record header.
		 * <header>
      	 *   <identifier>oai:daac.ornl.gov:ornldaac_1</identifier>
         *   <datestamp>2009-07-21T22:28:58Z</datestamp>
	     * </header>
		 */
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
				for (final Record record : records) notify(record);
			
			}
		}
				
	}
	

}
