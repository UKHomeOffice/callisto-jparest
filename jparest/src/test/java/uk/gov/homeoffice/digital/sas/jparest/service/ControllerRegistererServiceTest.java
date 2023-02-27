package uk.gov.homeoffice.digital.sas.jparest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import uk.gov.homeoffice.digital.sas.jparest.controller.ResourceApiController;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityA;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityB;

@ExtendWith(MockitoExtension.class)
class ControllerRegistererServiceTest {

  @Mock
  private RequestMappingHandlerMapping requestMappingHandlerMapping;

  @Mock
  private BaseEntityCheckerService baseEntityCheckerService;

  @Mock
  private ResourceApiController<?> resourceApiController;

  private ControllerRegistererService controllerRegistererService;


  @BeforeEach
  public void setup() {
    controllerRegistererService = new ControllerRegistererService(
        requestMappingHandlerMapping, baseEntityCheckerService);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "dummyEntityAs",
      "dummyEntityBs",
      "dummyentityc",
      "dummyentityd"
  })
  void mapRestOperationsToController_controllerAndPathProvided_registersRestfulEndpoints(String resourceName) {

    var expectedCalls = List.of(
        List.of("{GET [/resources/" + resourceName + "], produces [application/json]}", "list"),
        List.of("{GET [/resources/" + resourceName + "/{id}], produces [application/json]}", "get"),
        List.of("{POST [/resources/" + resourceName + "], produces [application/json]}", "create"),
        List.of("{DELETE [/resources/" + resourceName + "/{id}], produces [application/json]}", "delete"),
        List.of("{PUT [/resources/" + resourceName + "/{id}], produces [application/json]}", "update"),
        List.of("{PUT [/resources/" + resourceName + "/batch-update], produces [application/json]}", "batchUpdate"));

    var resourceEndpointPaths = new ArrayList<String>();
    assertThatNoException().isThrownBy(() -> controllerRegistererService.mapRestOperationsToController(
        "resources/" + resourceName,
        resourceApiController,
        resourceEndpointPaths::add
    ));
    verifyExpectedHandlerMappingCalls(expectedCalls);
    assertThat(resourceEndpointPaths)
        .hasSize(1).allSatisfy(resourceEndpointPath ->
            assertThat(resourceEndpointPath).isEqualTo("resources/" + resourceName));
  }


  @Test
  void registerRelatedPaths_controllerAndPathAndEntityUtilsProvided_registersRelatedEndpoints() {
    var expectedCalls = List.of(
        List.of("{GET [/resources/dummyEntityAs/{id}/{relation:\\QdummyEntityBSet\\E}], produces [application/json]}",
            "getRelated"),
        List.of("{DELETE [/resources/dummyEntityAs/{id}/{relation:\\QdummyEntityBSet\\E}/{relatedIds}], produces [application/json]}",
            "deleteRelated"),
        List.of("{PUT [/resources/dummyEntityAs/{id}/{relation:\\QdummyEntityBSet\\E}/{relatedIds}], produces [application/json]}",
            "addRelated"));

    when(baseEntityCheckerService.isBaseEntitySubclass(any())).thenReturn(true);

    var relatedResourceEndpointPaths = new HashMap<Class<?>, String>();

    assertThatNoException().isThrownBy(() -> controllerRegistererService.registerRelatedPaths(
        "resources/dummyEntityAs",
        DummyEntityA.class,
        resourceApiController,
        relatedResourceEndpointPaths::put
    ));
    verifyExpectedHandlerMappingCalls(expectedCalls);
    assertThat(relatedResourceEndpointPaths).containsExactlyEntriesOf(
        Map.of(DummyEntityB.class, "resources/dummyEntityAs/{id}/dummyEntityBSet"));
  }


  private void verifyExpectedHandlerMappingCalls(List<List<String>> expectedCalls) {
    for (var expected : expectedCalls) {
      Mockito.verify(requestMappingHandlerMapping).registerMapping(
          argThat(requestMappingInfo -> requestMappingInfo.toString().equals(expected.get(0))),
          argThat(controller -> controller.equals(resourceApiController)),
          argThat(method -> method.getName().equals(expected.get(1))));
    }
  }

}
