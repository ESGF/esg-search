package esg.search.harvest.impl;

import esg.search.harvest.api.MetadataRepositoryCrawler;

/**
 * Abstract class for crawling a metadata repository, 
 * producing metadata records, and notifying all subscribed consumers.
 * The production/notification functionality is derived from the {@link RecordProducerImpl} superclass.
 */
public abstract class MetadataHarvester extends RecordProducerImpl implements MetadataRepositoryCrawler {}
