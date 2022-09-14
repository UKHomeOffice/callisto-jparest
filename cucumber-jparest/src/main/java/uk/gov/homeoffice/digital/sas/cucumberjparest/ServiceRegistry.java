package uk.gov.homeoffice.digital.sas.cucumberjparest;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * Registry for services and their location
 * 
 */
public class ServiceRegistry {
    private Map<String, String> services = new HashMap<>();

    public ServiceRegistry() {
    }

    
    /** 
     * 
     * Adds the location for the given service
     * 
     * @param name The name of the service
     * @param url The URL of the location
     * @return String
     */
    public void addService(String name, String url) {
        if (services.containsKey(name)){
            throw new IllegalArgumentException(name + " already exists");
        }
        services.put(name, url);
    }

    
    /** 
     * 
     * Retrieves the URL for the given service.
     * 
     * @param name The name of the regsitered service to return the URL of.
     * @return String The URL for the given service
     */
    public String getService(String name) {
        if (!services.containsKey(name)){
            throw new IllegalArgumentException(name + " does not exist");
        }
        return services.get(name);
    }
}
