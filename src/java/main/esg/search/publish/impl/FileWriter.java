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
package esg.search.publish.impl;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import esg.search.core.Record;
import esg.search.publish.api.RecordConsumer;
import esg.search.publish.impl.solr.SolrXmlBuilder;

/**
 * Implementation of {@link RecordConsumer} that writes the serialized record XML to the file system.
 */
public class FileWriter implements RecordConsumer {
	
	/**
	 * The directory where the serialized records are written.
	 */
	private final File directory;
		
	private static final Log LOG = LogFactory.getLog(FileWriter.class);
	
	private SolrXmlBuilder serializer = new SolrXmlBuilder();
	
	public FileWriter(final File directory) {
		
		Assert.isTrue(directory.exists(),"Directory: "+directory.getAbsolutePath()+" does not exist");
		this.directory = directory;
		
	}

	/**
	 * {@inheritDoc}
	 */
	public void consume(final Record record) throws Exception {
		
		final File file = new File(directory, record.getId()+".xml");
		if (LOG.isInfoEnabled()) LOG.info("Indexing record:"+record.getId()+" to file:"+file.getAbsolutePath());
		final String xml = serializer.buildAddMessage(record, true);
		FileUtils.writeStringToFile(file, xml);
		
	}

}
