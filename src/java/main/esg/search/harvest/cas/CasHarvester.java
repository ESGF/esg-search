package esg.search.harvest.cas;

import java.net.URI;
import java.util.List;

import org.jdom.Document;

import esg.search.core.Record;
import esg.search.harvest.impl.MetadataHarvester;
import esg.search.harvest.xml.MetadataHandler;
import esg.search.utils.HttpClient;
import esg.search.utils.XmlParser;

/**
 * Class to harvest metadata from a remote CAS server.
 */
public class CasHarvester extends MetadataHarvester {
		
	private final MetadataHandler metadataHandler;
	
	public CasHarvester(final MetadataHandler metadataHandler) {
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
		final List<Record> records = metadataHandler.parse(doc.getRootElement());
		
		// index records
		for (final Record record : records) notify(record);

	}

}
