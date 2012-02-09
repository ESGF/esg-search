package esg.search.publish.api;

import java.io.Serializable;

public class PublishingException extends RuntimeException implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public PublishingException(final String message) {
        super(message);
    }

}
