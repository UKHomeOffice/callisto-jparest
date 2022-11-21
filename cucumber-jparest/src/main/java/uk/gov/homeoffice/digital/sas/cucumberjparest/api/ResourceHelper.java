package uk.gov.homeoffice.digital.sas.cucumberjparest.api;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.homeoffice.digital.sas.cucumberjparest.persona.Persona;
import uk.gov.homeoffice.digital.sas.cucumberjparest.persona.PersonaManager;

@Component
public class ResourceHelper {

  private final JpaRestApiClient jpaRestApiClient;

  private final PersonaManager personaManager;

  @Autowired
  public ResourceHelper(JpaRestApiClient jpaRestApiClient, PersonaManager personaManager) {
    this.jpaRestApiClient = jpaRestApiClient;
    this.personaManager = personaManager;
  }

  public String getResourceId(String personaName, String service, String resourceType,
      String filter) {
    Persona persona = personaManager.getPersona(personaName);
    Map<String, String> params = new HashMap<>();
    params.put("filter", filter);
    // TODO assert getBody().items.size()==1
    return jpaRestApiClient.retrieve(persona, service, resourceType, params)
            .getResponse().getBody().jsonPath().get("items[0].id");
  }
}
