package esg.search.publish.api;

/**
 * Interface for exposing the publishing operations to web service clients.
 * This interface combines the methods of the {@link PublishingService} and {@link LegacyPublishingService}.
 * 
 * @author Luca Cinquini
 *
 */
public interface RemotePublishingService extends PublishingService, LegacyPublishingService {

}
