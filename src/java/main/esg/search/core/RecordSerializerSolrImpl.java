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
package esg.search.core;

import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import esg.search.query.impl.solr.SolrXmlPars;

public class RecordSerializerSolrImpl implements RecordSerializer {
	
	private final static String NEWLINE  = System.getProperty("line.separator");
	
	/**
	 * {@inheritDoc}
	 */
	public String serialize(final Record record, final boolean indent) {
		
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
	
	/**
	 * {@inheritDoc}
	 * @param doc
	 * @return
	 */
	public Record deserialize(final Element doc) {
		
		final Record record = new RecordImpl();
		
		/*
		 * 	<str name="url">http://localhost/access?id=1</str>
			<arr name="description">
				<str>description #1</str>
			</arr>
		 */
		for (final Object child : doc.getChildren()) {
			final Element element = (Element)child;
			final String elName = element.getName();
			final String nameAttValue = element.getAttributeValue(SolrXmlPars.ATTRIBUTE_NAME);
			
			// multi-valued field
			// <arr name="...">....</arr>
			if (elName.equals(SolrXmlPars.ELEMENT_ARR)) {
				for (final Object obj : element.getChildren()) {
					deserializeElement(nameAttValue, (Element)obj, record);
				}
				
			// single-valued field
			} else {
				deserializeElement(nameAttValue, element, record);
			}
			
		
		}

		return record;
		
	}
	
	private void deserializeElement(final String fieldName, final Element element, final Record record) {
		
		final String value = element.getTextNormalize();
		if (fieldName.equals(SolrXmlPars.FIELD_ID)) {
			record.setId(value);
		} else {
			record.addField(fieldName, value);
		}

	}
	
	private String toString(final Element element, final boolean indent) {
	  	Format format = (indent ? Format.getPrettyFormat() : Format.getCompactFormat());
	  	XMLOutputter outputter = new XMLOutputter(format);
	  	return outputter.outputString(element) + (indent ? NEWLINE : "");

	}

}
