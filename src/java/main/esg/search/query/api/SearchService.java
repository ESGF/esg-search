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


/**
 * Interface defining the API for mix faceted/text search.
 * The search criteria are completely contained in the bean {@link SearchInput},
 * and the search results in the bean {@link SearchOutput}. The two boolean parameters "getResults" and "getFacets"
 * determine whether the search operation is executed to return search results, a new set of search facets subject to
 * the constrained contained in {@link SearchInput}, or both.
 */
public interface SearchService {
	
	/**
	 * Method to search for results and/or facets matching the given constraints.
	 * 
	 * @param input
	 * @param getResults : true to search for results
	 * @param getFacets : true to search for facets
	 * @return
	 * @throws Exception
	 */
	public SearchOutput search(final SearchInput input, final boolean getResults, final boolean getFacets) throws Exception;
		

}
