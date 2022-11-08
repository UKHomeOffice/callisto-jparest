package uk.gov.homeoffice.digital.sas.cucumberjparest.stepdefinitions;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
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
import uk.gov.homeoffice.digital.sas.cucumberjparest.HttpResponseManager;
import uk.gov.homeoffice.digital.sas.cucumberjparest.Interpolation;
import uk.gov.homeoffice.digital.sas.cucumberjparest.JpaRestApiClient;
import uk.gov.homeoffice.digital.sas.cucumberjparest.PersonaManager;
import uk.gov.homeoffice.digital.sas.cucumberjparest.ScenarioState;

@ExtendWith(MockitoExtension.class)
class CucumberConfigTest {

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

  private CucumberConfig cucumberConfig;

  @BeforeEach
  void setup() throws ClassNotFoundException {
    cucumberConfig = new CucumberConfig(personaManager,
        httpResponseManager,
        jpaRestApiClient,
        objectMapper,
        scenarioState,
        interpolation);

    Mockito.when(objectMapper.getTypeFactory()).thenReturn(typeFactory);
    Mockito.when(typeFactory.findClass(any(String.class))).thenThrow(new ClassNotFoundException());

  }

//  void renameMe1() {
//    String type = "";
//    AssertionError thrown = assertThrows(
//        AssertionError.class,
//        () -> cucumberConfig.resolveType(type),
//        "Expected resolveType() to throw an exception, but it didn't"
//    );
//    assertThat(thrown.getMessage(),
//        is("Unknown type ''. To configure use JpaTestContext.put(\"\", FullyQualifiedTypeName.class);"));
//  }

  @Test
  void renameMe2() {


    String type = "package.UnknownClass";
    AssertionError thrown = assertThrows(
        AssertionError.class,
        () -> cucumberConfig.resolveType(type),
        "Expected resolveType() to throw an exception, but it didn't"
    );

    assertThat(thrown.getMessage(),
        is("Unknown type 'package.UnknownClass'. To configure use JpaTestContext.put(\"package.UnknownClass\", FullyQualifiedTypeName.class);"));
  }
}