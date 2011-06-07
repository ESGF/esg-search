package esg.search.publish.impl.solr;

import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import esg.search.core.Record;
import esg.search.query.impl.solr.SolrXmlPars;

/**
 * Utility class to generate Solr XML messages.
 * @author luca.cinquini
 *
 */
public class SolrXmlBuilder {
	
	private final static String NEWLINE  = System.getProperty("line.separator");
	
	/**
	 * Method to create an XML message to delete records with given ids.
	 * Note that this method also removed all records that declare that record as a <i>parent</i> record.
	 * Example output XML: <delete><id>05991</id><query>parent_id:05991</query><id>06544</id><query>parent_id:06544</query></delete>
	 * @param ids
	 */
	public String buildDeleteMessage(final List<String> ids, final boolean indent) {
		
		// <delete>
		final Element deleteEl = new Element(SolrXmlPars.ELEMENT_DELETE);
		
		// loop over record identifiers
		for (final String id : ids) {
		    
			// <id>...</id>
			final Element idEl = new Element(SolrXmlPars.ELEMENT_ID);
			idEl.setText(id);
			deleteEl.addContent(idEl);
			
			// <query>parent_id:...</query>
			final Element queryEl = new Element(SolrXmlPars.ELEMENT_QUERY);
			queryEl.setText(SolrXmlPars.FIELD_PARENT_ID+":"+id);
			deleteEl.addContent(queryEl);
			
		}
		
		return toString(deleteEl, indent);
		
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
	public String buildAddMessage(final Record record, final boolean indent) {
		
		// <add>
		final Element addEl = new Element(SolrXmlPars.ELEMENT_ADD);
		
		// <doc>
		final Element docEl = new Element(SolrXmlPars.ELEMENT_DOC);
		addEl.addContent(docEl);
		
		// <field name="id">...</field>
		final Element idEl = new Element(SolrXmlPars.ELEMENT_FIELD);
		idEl.setAttribute(SolrXmlPars.ATTRIBUTE_NAME, SolrXmlPars.FIELD_ID);
		idEl.setText(record.getId());
		docEl.addContent(idEl);
		
		// <field name="...">....</field>
		// (for each value)
		final Map<String, List<String>> fields = record.getFields();
		for (final String key : fields.keySet()) {
			for (final String value : fields.get(key)) {
				final Element fieldEl = new Element(SolrXmlPars.ELEMENT_FIELD);
				fieldEl.setAttribute(SolrXmlPars.ATTRIBUTE_NAME, key);
				fieldEl.setText(value);
				docEl.addContent(fieldEl);
			}
		}

		return toString(addEl, indent);

	}
	
	private String toString(final Element element, final boolean indent) {
	  	Format format = (indent ? Format.getPrettyFormat() : Format.getCompactFormat());
	  	XMLOutputter outputter = new XMLOutputter(format);
	  	return outputter.outputString(element) + (indent ? NEWLINE : "");
	}

}
