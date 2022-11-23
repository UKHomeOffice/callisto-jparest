package uk.gov.homeoffice.digital.sas.cucumberjparest.api;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import uk.gov.homeoffice.digital.sas.cucumberjparest.utils.SerialisationUtil;

/**
* Registry for services and their location.
 */
@Getter
@Setter
@Component
public class ServiceRegistry {

  public static final String SERVICE_REGISTRY_SYSTEM_PROPERTY_NAME
      = "cucumber.jparest.serviceRegistry";

  public ServiceRegistry() {
    String serviceRegistryString = System.getProperty(SERVICE_REGISTRY_SYSTEM_PROPERTY_NAME);
    this.services = SerialisationUtil.stringToMap(serviceRegistryString);
  }

  private Map<String, String> services;

  /**
   * Retrieves the URL for the given service.
   *
   * @param name The name of the registered service to return the URL of.
   * @return String The URL for the given service
   */
  public String getService(String name) {
    if (!services.containsKey(name)) {
      throw new IllegalArgumentException(name + " does not exist");
    }
    return services.get(name);
  }
}
