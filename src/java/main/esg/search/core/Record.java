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

/**
 * Interface representing a search record,
 * both as item to be indexed into the search repository, and to be returned as a search result.
 * This interface leaves the record attributes as completely generic fields, 
 * except for the record id and version.
 */
public interface Record {
	
	/**
	 * Method to return the record's unique identifier.
	 * @return
	 */
	String getId();

	/**
	 * Method to assign the record's unique identifier;
	 */
	void setId(String id);

	/**
	 * Method to add a field (name, value) pair to the record.
	 * @param name
	 * @param value
	 */
	void addField(final String name, final String value);
	
	/**
	 * Method to return an (unmodifiable) map of multi-valued fields for this record.
	 * @return
	 */
	Map<String, List<String>> getFields();
	
	/**
	 * Method to return the first value of a named field, or null if not available
	 * @return
	 */
	String getField(String name);
	
	/**
	 * Method to return the record version, used to only index the latest version.
	 * Dates can be converted to milliseconds from the Epoch for versioning.
	 * @return
	 */
	long getVersion();
	
	/**
	 * Method to assign a version to the record.
	 * @param version
	 */
	//void setVersion(Comparable<?> version);

}
