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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import esg.search.query.impl.solr.SolrXmlPars;


/**
 * Standard bean implementation of the {@link Record} interface.
 */
public class RecordImpl implements Record, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * The record unique identifier.
	 */
	String id;

	/**
	 * A map of multi-valued fields applicable to this record.
	 * The map is ordered on keys to allow for easier testing.
	 */
	final Map<String,List<String>> fields = new TreeMap<String, List<String>>();
	
	//private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	/**
	 * Constructor for yet unknown record identifier.
	 */
	public RecordImpl() {}
	
	/**
	 * Constructor for known unique record identifier.
	 * @param id
	 */
	public RecordImpl(String id) {
		this.id = id;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Map<String, List<String>> getFields() {
		return Collections.unmodifiableMap(fields);
	}
	
	/**
	 * This implementation retrieves the record version for the same named field.
	 */
	public long getVersion() {
		String version = getFieldValue(SolrXmlPars.FIELD_VERSION); 
		if ( hasText(version) ) {
			try {
				return Long.parseLong(version);
			} catch(NumberFormatException e) {}
		}
		return 0; // no version available
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Note that this implementation does not add pairs where the key or the value is null or blank.
	 */
	public void addField(final String name, final String value) {
		
		if (this.hasText(name) && this.hasText(value)) {
			if (!fields.containsKey(name)) {
				fields.put(name, new ArrayList<String>());
			}
			fields.get(name).add(value);
		}
		
	}
	
	private boolean hasText(final String s) {
		return s!=null && s.trim().length()>0;
	}
	
	public String toString() {
		
		final StringBuilder sb = new StringBuilder();
		sb.append("Record ID="+id);
		for (final String key : fields.keySet()) {
			sb.append(" [field name="+key+" values="+fields.get(key)+"] ");
		}
		return sb.toString();
		
	}
	
	/**
	 * Method to return the first value of a name field, or null if the field is not set.
	 * @param name
	 * @return
	 */
	public String getFieldValue(final String name) {
		return ( fields.get(name) !=null && fields.get(name).size() > 0 ? fields.get(name).get(0) : null );
	}

}
