package uk.gov.homeoffice.digital.sas.cucumberjparest.scenarios.parametertypes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.homeoffice.digital.sas.cucumberjparest.api.HttpResponseManager;
import uk.gov.homeoffice.digital.sas.cucumberjparest.api.JpaRestApiClient;
import uk.gov.homeoffice.digital.sas.cucumberjparest.persona.PersonaManager;
import uk.gov.homeoffice.digital.sas.cucumberjparest.scenarios.ScenarioState;
import uk.gov.homeoffice.digital.sas.cucumberjparest.scenarios.interpolation.Interpolation;

@ExtendWith(MockitoExtension.class)
class ParameterTypesTest {

  @Mock
  private PersonaManager personaManager;
  @Mock
  private HttpResponseManager httpResponseManager;
  @Mock
  private JpaRestApiClient jpaRestApiClient;
  @Mock
  private ScenarioState scenarioState;
  @Mock
  private ObjectMapper objectMapper;
  @Mock
  private Interpolation interpolation;
  @Mock
  private TypeFactory typeFactory;

  private ParameterTypes parameterTypes;

  @BeforeEach
  void setup() throws ClassNotFoundException {
    parameterTypes = new ParameterTypes(personaManager,
        httpResponseManager,
        jpaRestApiClient,
        objectMapper,
        scenarioState,
        interpolation);

    Mockito.when(objectMapper.getTypeFactory()).thenReturn(typeFactory);
    Mockito.when(typeFactory.findClass(any(String.class))).thenThrow(new ClassNotFoundException());

  }

  @Test
  void shouldThrowAnErrorIfTypeCannotBeResolved() {
    String type = "package.UnknownClass";
    AssertionError thrown = assertThrows(
        AssertionError.class,
        () -> parameterTypes.resolveType(type),
        "Expected resolveType() to throw an exception, but it didn't"
    );

    assertThat(thrown.getMessage())
        .isEqualTo(
            "Unknown type 'package.UnknownClass'. To configure use JpaTestContext.put(\"package.UnknownClass\", FullyQualifiedTypeName.class);"
        );
  }
}