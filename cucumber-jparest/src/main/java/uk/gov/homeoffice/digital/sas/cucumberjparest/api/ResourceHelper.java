package uk.gov.homeoffice.digital.sas.cucumberjparest.api;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.response.ResponseBody;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.homeoffice.digital.sas.cucumberjparest.persona.Persona;
import uk.gov.homeoffice.digital.sas.cucumberjparest.persona.PersonaManager;

@Component
@SuppressWarnings("squid:S5960")
public class ResourceHelper {

  private final JpaRestApiClient jpaRestApiClient;

  private final PersonaManager personaManager;

  @Autowired
  public ResourceHelper(JpaRestApiClient jpaRestApiClient, PersonaManager personaManager) {
    this.jpaRestApiClient = jpaRestApiClient;
    this.personaManager = personaManager;
  }

  /**
   * Returns unique and non-empty resource ID for the specified arguments.
   *
   * @param personaName  Person name, e.g. Trevor
   * @param service      Service name, e.g. timecard
   * @param resourceType Resource type, e.g. time-period-types
   * @param filter       filter to be applied, e.g. name="Shift"
   * @return Unique and non-empty resource ID
   */
  public String getResourceId(String personaName, String service, String resourceType,
      String filter) {
    Persona persona = personaManager.getPersona(personaName);
    Map<String, String> params = new HashMap<>();
    params.put("filter", filter);

    ResponseBody responseBody = jpaRestApiClient.retrieve(persona, service, resourceType, params)
        .getResponse().getBody();

    Integer numberOfItems = responseBody.jsonPath().get("items.size()");
    // Response must have one and only one item
    assertThat(numberOfItems).isEqualTo(1);

    String resourceId = responseBody.jsonPath().get("items[0].id");
    // Item must have a non-empty id
    assertThat(resourceId).isNotBlank();

    return resourceId;
  }
}
