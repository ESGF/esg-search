package esg.search.publish.impl.solr;

import java.util.List;

import org.jdom.Element;

import esg.search.core.Record;
import esg.search.core.RecordSerializer;
import esg.search.query.api.QueryParameters;
import esg.search.query.impl.solr.SolrXmlPars;
import esg.search.utils.XmlUtils;

/**
 * Utility class to generate Solr XML messages.
 * 
 * The bulk of the Java - XML record conversion is executed by the @see {@link RecordSerializer}.
 * 
 * @author luca.cinquini
 *
 */
public class SolrMessageBuilder {
		
	private final static RecordSerializer serializer = new SolrRecordSerializer();
	
	/**
	 * Method to create an XML message to delete records with given ids.
	 * 
	 * Note that this method also removes all records that declare that record as a <i>parent</i> record.
	 * Example output XML: <delete><id>05991</id><query>dataset_id:05991</query><id>06544</id><query>dataset_id:06544</query></delete>
	 * @param ids
	 */
	public static String buildDeleteMessage(final List<String> ids, final boolean indent) {
		
		// <delete>
		final Element deleteEl = new Element(SolrXmlPars.ELEMENT_DELETE);
		
		// loop over record identifiers
		for (final String id : ids) {
		    
		    // escape Lucene/Solr reserved characters: + - && || ! ( ) { } [ ] ^ " ~ * ? : \ /
		    //String _id = id.replaceAll(":", "\\\\:");
		    
			// <id>...</id>
			final Element idEl = new Element(SolrXmlPars.ELEMENT_ID);
			idEl.setText(id);
			deleteEl.addContent(idEl);
			
			// <query>dataset_id:...</query>
			final Element queryEl = new Element(SolrXmlPars.ELEMENT_QUERY);
			queryEl.setText(QueryParameters.FIELD_DATASET_ID+":"+id);
			deleteEl.addContent(queryEl);
			
		}
		
		return XmlUtils.toString(deleteEl, indent);
		
	}
	
	/**
	 * Method to create an XML message to add a given record.
	 * Example output XML:
	 *  <add>
  	 *  	<doc>
     *			<field name="id">test id</field>
     *			<field name="description">test description</field>
     *			<field name="property">value A</field>
     * 			<field name="property">value B</field>
     *			<field name="title">test title</field>
     * 			<field name="type">Dataset</field>
     *		 	<field name="url">http://test.com/</field>
     * 			<field name="version">1</field>
     * 		</doc>
	 *	</add>
	 */
	public static String buildAddMessage(final Record record, final boolean indent) {
		
		// <add>
		final Element addEl = new Element(SolrXmlPars.ELEMENT_ADD);
		
		// <doc>
		final Element docEl = serializer.serialize(record);
		addEl.addContent(docEl);
		
		return XmlUtils.toString(addEl, indent);

	}
	
	/**
	 * Method that returns the Solr commit instruction.
	 * @param ids
	 * @param indent
	 * @return
	 */
	public static String buildCommitMessage() {
	    return "<commit />";
	}
	
	/**
     * Method that returns the Solr optimize instruction.
     * @param ids
     * @param indent
     * @return
     */
    public static String buildOptimizeMessage() {
        return "<optimize/>";
    }
	
	private SolrMessageBuilder() {}

}
