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
package esg.search.query.api;

import java.util.List;

/**
 * Interface representing a search category, 
 * i.e. an index space that can be used to constraint the search results.
 */
public interface Facet {
	
	/**
	 * Returns the facet key used for programmatic access (immutable).
	 * @return
	 */
	public String getKey();
	
	/**
	 * Returns the facet label exposed to human users (immutable).
	 * @return
	 */
	public String getLabel();
	
	/**
	 * Returns the optional facet description for human users (immutable).
	 * @return
	 */
	public String getDescription();
	
	/**
	 * Returns the current count of records associated with this facet.
	 * @return
	 */
	public int getCounts();
	
	/**
	 * Method to set the current record count.
	 */
	public void setCounts(int counts);
	
	/**
	 * Returns a list of sub-facets for the current facet.
	 * @return
	 */
	public List<Facet> getSubFacets();
	
	/**
	 * Adds a sub-facet to the current facet.
	 * @param subFacet
	 */
	public void addSubFacet(Facet subFacet);

}
