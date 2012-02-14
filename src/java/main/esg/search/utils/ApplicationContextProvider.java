package esg.search.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * This class is deployed within the Spring application context 
 * and automatically injected with a reference to the context at initialization time.
 * It then makes the context available to non-Spring classes via its static method
 * 
 * @author Luca Cinquini
 *
 */
public class ApplicationContextProvider implements ApplicationContextAware {
    
    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        ApplicationContextProvider.context = context;
    }
    
    /**
     * Method that makes the Spring application context available to external classes.
     * 
     * @return
     */
    public static ApplicationContext getApplicationContext() {
        return ApplicationContextProvider.context;
    }

}
