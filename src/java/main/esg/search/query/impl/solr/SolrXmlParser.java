package esg.search.query.impl.solr;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import esg.search.core.Record;
import esg.search.core.RecordSerializer;
import esg.search.core.RecordSerializerSolrImpl;
import esg.search.query.api.Facet;
import esg.search.query.api.SearchInput;
import esg.search.query.api.SearchOutput;
import esg.search.utils.XmlParser;

/**
 * Utility class to parse Solr XML files into information java objects.
 */
public class SolrXmlParser {
	
	/**
	 * The underlying XML parser.
	 */
	private final XmlParser xmlParser;
	
	/**
	 * The record-XML serializer/deserializer.
	 */
	private final RecordSerializer serializer = new RecordSerializerSolrImpl();
	
	/**
	 * Constructor instantiates the XML parser.
	 */
	public SolrXmlParser() {
		// no validation since Solr documents don't have a schema
		xmlParser = new XmlParser(false);
	}
	
	/**
	 * Method to retrieve the list of facets and counts from the <lst name="facet_counts"> snippet.
	 * @param xml
	 * @return
	 * @throws IOException
	 * @throws JDOMException
	 */
	public Map<String, Facet> parseFacets(final String xml, final SearchInput input) throws IOException, JDOMException {
		
		final Map<String,Facet> facets = new LinkedHashMap<String, Facet>();
		
		/* 
		<lst name="facet_counts">
		 	<lst name="facet_queries"/>
		 		<lst name="facet_fields">
		  			<lst name="project">
						<int name="AIRS">4</int>
						<int name="IPCC5">3</int>
						<int name="MLS">3</int>
		  		</lst>
		 	</lst>
		 	<lst name="facet_dates"/>
		</lst>
		*/
		final Document doc = xmlParser.parseString(xml);
		final Element root = doc.getRootElement();
		for (final Object lstEl : root.getChildren(SolrXmlPars.ELEMENT_LST)) {
			final Element _lstEl = (Element)lstEl;
			if (_lstEl.getAttribute(SolrXmlPars.ATTRIBUTE_NAME).getValue().equals(SolrXmlPars.ELEMENT_FACET_COUNTS)) {
				for (final Object ffEl : _lstEl.getChildren("lst")) {
					final Element _ffEl = (Element)ffEl;
					if (_ffEl.getAttributeValue(SolrXmlPars.ATTRIBUTE_NAME).equals(SolrXmlPars.ELEMENT_FACET_FIELDS)) {
					
						for (final Object flstEl : _ffEl.getChildren(SolrXmlPars.ELEMENT_LST)) {
							final Element _flstEl = (Element)flstEl;
							final String facetName = _flstEl.getAttributeValue(SolrXmlPars.ATTRIBUTE_NAME);
							final Facet facet = new FacetImpl(facetName, facetName, "");
							
							// constrained facet -> set one option to constraint value
							if (input.getConstraints().containsKey(facetName)) {
								
								final String subFacetName = input.getConstraints().get(facetName).get(0);
								final Facet subFacet = new FacetImpl(subFacetName, subFacetName, "");
								subFacet.setCounts(1);
								facet.addSubFacet( subFacet );
							
							// facet not constrained -> retrieve all options from XML response
							} else {
							
								for (final Object intEl : _flstEl.getChildren(SolrXmlPars.ELEMENT_INT)) {
									final Element _intEl = (Element)intEl;
									final String subFacetName = _intEl.getAttributeValue(SolrXmlPars.ATTRIBUTE_NAME);
									final int subFacetCounts = Integer.parseInt(_intEl.getText());
									if (subFacetCounts>0) {
										final Facet subFacet = new FacetImpl(subFacetName, subFacetName, "");
										subFacet.setCounts(subFacetCounts);
										facet.addSubFacet( subFacet );
									}
								}
							
							}
							
							facets.put(facetName, facet);
						}
					}	
				}
			}
		}
		
		return facets;

	}
	
	/**
	 * Method to retrieve the search results from the <result name="response" numFound="..." start="0"> snippet.
	 * 
	 * @param xml
	 * @return
	 * @throws IOException
	 * @throws JDOMException
	 */
	public SearchOutput parseResults(final String xml) throws IOException, JDOMException {
		
		final SearchOutput output = new SearchOutputImpl();
		final Document doc = xmlParser.parseString(xml);
		final Element root = doc.getRootElement();
		for (final Object resultEl : root.getChildren(SolrXmlPars.ELEMENT_RESULT)) {
			final Element _resultEl = (Element)resultEl;
			if (_resultEl.getAttributeValue(SolrXmlPars.ATTRIBUTE_NAME).equals(SolrXmlPars.ATTRIBUTE_VALUE_RESPONSE)) {
				
				final int numFound = Integer.parseInt(_resultEl.getAttributeValue(SolrXmlPars.ATTRIBUTE_NUM_FOUND));
				output.setCounts(numFound);
				final int start = Integer.parseInt(_resultEl.getAttributeValue(SolrXmlPars.ATTRIBUTE_START));
				output.setOffset(start);

				for (final Object docEl : _resultEl.getChildren(SolrXmlPars.ELEMENT_DOC)) {
					final Element _docEl = (Element)docEl;
					final Record record = serializer.deserialize(_docEl);
					output.addResult(record);
				}
				
			}
			
		}
		
		return output;
		
	}
	
}
