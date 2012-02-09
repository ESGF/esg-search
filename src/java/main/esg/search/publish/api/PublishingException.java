package esg.search.publish.api;

import java.io.Serializable;
import java.rmi.RemoteException;

public class PublishingException extends RemoteException implements Serializable {
    
    private static final long serialVersionUID = 1L;

    public PublishingException() {
        super();
    }
    
    public PublishingException(final String message) {
        super(message);
    }

}
