package uk.gov.homeoffice.digital.sas.cucumberjparest;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

/**
* Registry for services and their location.
 */
@Getter
@Setter
@Component
@AllArgsConstructor
public class ServiceRegistry {

  public static final String SERVICE_REGISTRY_SYSTEM_PROPERTY_NAME
      = "cucumber.jparest.serviceRegistry";
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
