package uk.gov.homeoffice.digital.sas.cucumberjparest.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static uk.gov.homeoffice.digital.sas.cucumberjparest.api.ResourceHelper.ITEMS_SIZE_JSON_PATH;
import static uk.gov.homeoffice.digital.sas.cucumberjparest.api.ResourceHelper.ITEM_ID_JSON_PATH;
import static uk.gov.homeoffice.digital.sas.cucumberjparest.api.ResourceHelper.RESOURCE_MUST_BE_UNIQUE;

import io.restassured.path.json.JsonPath;
import java.text.MessageFormat;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.homeoffice.digital.sas.cucumberjparest.persona.Persona;
import uk.gov.homeoffice.digital.sas.cucumberjparest.persona.PersonaManager;

@ExtendWith(MockitoExtension.class)
class ResourceHelperTest {

  public static final String FILTER = "name=\"Shift\"";
  public static final Map<String, String> FILTER_MAP = Map.of("filter", FILTER);
  public static final String RESOURCE_TYPE = "time-period-types";
  public static final String SERVICE = "timecard";
  public static final String PERSONA_NAME = "Trevor";
  public static final String RESOURCE_ID = "resource-id";

  private final Persona persona = new Persona();

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private JpaRestApiClient jpaRestApiClient;

  @Mock
  private PersonaManager personaManager;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private JsonPath jsonPath;

  private ResourceHelper resourceHelper;

  @BeforeEach
  void setup() {
    resourceHelper = new ResourceHelper(jpaRestApiClient, personaManager);
    Mockito.when(personaManager.getPersona(PERSONA_NAME)).thenReturn(persona);
    Mockito.when(
        jpaRestApiClient.retrieve(eq(persona), eq(SERVICE), eq(RESOURCE_TYPE), eq(FILTER_MAP))
            .getResponse().getBody().jsonPath()).thenReturn(jsonPath);
  }

  @Test
  void getResourceId_uniqueResource_resourceIdReturned() {
    Mockito.when(jsonPath.get(ITEMS_SIZE_JSON_PATH)).thenReturn(1);
    Mockito.when(jsonPath.get(ITEM_ID_JSON_PATH)).thenReturn(RESOURCE_ID);

    String resourceId = resourceHelper.getResourceId(PERSONA_NAME, SERVICE, RESOURCE_TYPE, FILTER);

    assertThat(resourceId).isEqualTo(RESOURCE_ID);
  }

  @Test
  void getResourceId_noMatchingResource_featureIsFailed() {
    Mockito.when(jsonPath.get(ITEMS_SIZE_JSON_PATH)).thenReturn(0);

    AssertionError thrown = assertThrows(
        AssertionError.class,
        () -> resourceHelper.getResourceId(PERSONA_NAME, SERVICE, RESOURCE_TYPE, FILTER),
        "Expected getResourceId() to throw an exception, but it didn't"
    );

    assertThat(thrown.getMessage())
        .isEqualTo(MessageFormat.format(RESOURCE_MUST_BE_UNIQUE, 0));
  }
}