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
package esg.search.publish.api;

import java.net.URI;

/**
 * API for crawling a remote metadata repository (with optional recursion)
 * and call back a {@link RecordProducer} during the process.
 * Each implementation must declare the specific MetadataRepositoryType that it can handle
 * through the support() method.
 */
public interface MetadataRepositoryCrawler {
	
	/**
	 * Method to crawl the metadata repository available at some URI,
	 * and optionally follow the symbolic links encountered while crawling.
	 * @param uri : the starting URI of metadata repository
	 * @param recursive : true to recursively crawl the locations referenced by the starting location
	 * @param callback: a {@link RecordProducer} that is triggered every time a new Record is generated while crawling
	 * @param : a boolean flag indicating whether the repository is crawled for publishing (true) or un-pubishing (false)
	 */
	public void crawl(URI uri, boolean recursive, RecordProducer callback, boolean publish) throws Exception;
	
	/**
	 * Method to indicate the {@link MetadataRepositoryType} supported by this crawler.
	 * @return
	 */
	public MetadataRepositoryType supports();
	
	/**
	 * Method to set an optional listener to monitor the crawling operation.
	 * @param listener
	 */
	void setListener(MetadataRepositoryCrawlerListener listener);
	
	

}
