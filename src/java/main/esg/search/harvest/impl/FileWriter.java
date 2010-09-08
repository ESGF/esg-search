package esg.search.harvest.impl;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import esg.search.core.Record;
import esg.search.core.RecordSerializer;
import esg.search.core.RecordSerializerSolrImpl;
import esg.search.harvest.api.RecordConsumer;

/**
 * Implementation of {@link RecordConsumer} that writes the serialized record XML to the file system.
 */
public class FileWriter implements RecordConsumer {
	
	/**
	 * The directory where the serialized records are written.
	 */
	private final File directory;
		
	private static final Log LOG = LogFactory.getLog(FileWriter.class);
	
	private RecordSerializer serializer = new RecordSerializerSolrImpl();
	
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
		final String xml = serializer.serialize(record, true);
		FileUtils.writeStringToFile(file, xml);
		
	}

}
