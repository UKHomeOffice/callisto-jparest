package uk.gov.homeoffice.digital.sas.cucumberjparest.api;

import static org.assertj.core.api.Fail.fail;

import io.restassured.path.json.JsonPath;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.homeoffice.digital.sas.cucumberjparest.persona.Persona;
import uk.gov.homeoffice.digital.sas.cucumberjparest.persona.PersonaManager;

@Component
@SuppressWarnings("squid:S5960")
public class ResourceHelper {

  static final String ITEMS_SIZE_JSON_PATH = "items.size()";
  static final String ITEM_ID_JSON_PATH = "items[0].id";
  static final String RESOURCE_MUST_BE_UNIQUE = "Expected one resource but got {0} instead";

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
    if (filter != null) {
      params.put("filter", filter);
    }

    JsonPath jsonPath = jpaRestApiClient.retrieve(persona, service, resourceType, params)
        .getResponse().getBody().jsonPath();

    Integer numberOfItems = jsonPath.get(ITEMS_SIZE_JSON_PATH);
    // Response must have one and only one item
    if (!Integer.valueOf(1).equals(numberOfItems)) {
      fail(MessageFormat.format(RESOURCE_MUST_BE_UNIQUE, numberOfItems));
    }

    return jsonPath.get(ITEM_ID_JSON_PATH);
  }
}
