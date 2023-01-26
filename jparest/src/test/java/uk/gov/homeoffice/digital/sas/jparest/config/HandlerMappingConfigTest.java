package uk.gov.homeoffice.digital.sas.jparest.config;

import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.homeoffice.digital.sas.jparest.EntityUtils;
import uk.gov.homeoffice.digital.sas.jparest.ResourceEndpoint;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;
import uk.gov.homeoffice.digital.sas.jparest.controller.ResourceApiController;
import uk.gov.homeoffice.digital.sas.jparest.factory.ResourceApiControllerFactory;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityA;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityB;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityD;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityG;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;
import uk.gov.homeoffice.digital.sas.jparest.service.BaseEntityCheckerService;
import uk.gov.homeoffice.digital.sas.jparest.service.ControllerRegistererService;
import uk.gov.homeoffice.digital.sas.jparest.service.ResourceApiService;
import uk.gov.homeoffice.digital.sas.jparest.factory.ResourceApiServiceFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HandlerMappingConfigTest <T extends BaseEntity> {

  @Mock
  private ResourceEndpoint resourceEndpoint;

  @Mock
  private ResourceApiServiceFactory resourceApiServiceFactory;

  @Mock
  private ResourceApiControllerFactory resourceApiControllerFactory;

  @Mock
  private BaseEntityCheckerService baseEntityCheckerService;

  @Mock
  private ControllerRegistererService controllerRegistererService;

  @Mock
  private ResourceApiService<T> resourceApiService;

  @Mock
  private ResourceApiController<T> resourceApiController;

  @Captor
  ArgumentCaptor<Consumer<String>> addResourceConsumerCaptor;

  @Captor
  ArgumentCaptor<BiConsumer<Class<? extends BaseEntity>, String>> addRelatedResourceConsumerCaptor;

  private HandlerMappingConfig handlerMappingConfig;

  @BeforeEach
  public void setup() {
      handlerMappingConfig = new HandlerMappingConfig(
          resourceEndpoint,
          resourceApiServiceFactory,
          resourceApiControllerFactory,
          baseEntityCheckerService,
          controllerRegistererService);
  }

  @Test
  void configureResourceMapping_resourcesDiscovered_verifyInteractionWithControllerRegistererService()
      throws NoSuchMethodException {

    Map<Class<?>, String> baseEntitySubClassesMap = Map.of(
        DummyEntityA.class, "dummyEntityAs",
        DummyEntityG.class, "dummyEntityGs"
    );
    when(baseEntityCheckerService.filterBaseEntitySubClasses()).thenReturn(baseEntitySubClassesMap);

    baseEntitySubClassesMap.forEach((resourceClass, entityName) -> {
      when(baseEntityCheckerService.isBaseEntitySubclass(baseEntitySubClassesMap)).thenReturn((clazz) -> true);
      when(resourceApiServiceFactory.getBean(
          eq((Class<T>) resourceClass), any(EntityUtils.class))).thenReturn(resourceApiService);
      when(resourceApiControllerFactory.getBean((Class<T>) resourceClass, resourceApiService)).thenReturn(resourceApiController);
    });

    handlerMappingConfig.configureResourceMapping();

    baseEntitySubClassesMap.forEach((resourceClass, entityName) -> {
      try {
        String resourcePath = resourceClass.getAnnotation(Resource.class).path();
        verify(controllerRegistererService).mapRestOperationsToController(
            argThat(path -> path.contains(resourcePath)), eq(resourceApiController), any());

        verify(controllerRegistererService).registerRelatedPaths(argThat(path -> path.contains(resourcePath)),
            any(EntityUtils.class), eq(resourceApiController), any());
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
        fail("NoSuchMethodException thrown");
      }
    });
  }

  @Test
  void configureResourceMapping_resourcesDiscovered_resourceTypesAndPathsAddedToServiceForOpenApi()
      throws NoSuchMethodException {

    Map<Class<?>, String> baseEntitySubClassesMap = Map.of(
        DummyEntityA.class, "dummyEntityAs",
        DummyEntityG.class, "dummyEntityGs"
    );
    when(baseEntityCheckerService.filterBaseEntitySubClasses()).thenReturn(baseEntitySubClassesMap);
    baseEntitySubClassesMap.forEach((resourceClass, entityName) ->
        when(baseEntityCheckerService.isBaseEntitySubclass(baseEntitySubClassesMap)).thenReturn((clazz) -> true));

    handlerMappingConfig.configureResourceMapping();

    baseEntitySubClassesMap.forEach((resourceClass, entityName) -> {
      verify(resourceEndpoint).addResourceType(resourceClass);
      try {
        var resourcePath = resourceClass.getAnnotation(Resource.class).path();

        verify(controllerRegistererService).mapRestOperationsToController(
            argThat(path -> path.contains(resourcePath)), any(), addResourceConsumerCaptor.capture());
        addResourceConsumerCaptor.getValue().accept("path/" + resourcePath);
        verify(resourceEndpoint).add(resourceClass, "path/" + resourcePath);

        verify(controllerRegistererService).registerRelatedPaths(
            argThat(path -> path.contains(resourcePath)), any(), any(), addRelatedResourceConsumerCaptor.capture());
        var relatedResourcePath = DummyEntityB.class.getAnnotation(Resource.class).path();
        addRelatedResourceConsumerCaptor.getValue().accept(DummyEntityB.class, "relatedPath/" + relatedResourcePath);
        verify(resourceEndpoint).addRelated((Class<T>) resourceClass, DummyEntityB.class,
            "relatedPath/" + relatedResourcePath);

      } catch (NoSuchMethodException e) {
        e.printStackTrace();
        fail("NoSuchMethodException thrown");
      }
    });
  }

  @Test
  void configureResourceMapping_entityResourceAnnotationDoesNotHavePathValue_pathObtainedFromEntityName()
      throws NoSuchMethodException {

    Map<Class<?>, String> baseEntitySubClassesMap = Map.of(DummyEntityD.class, "dummyEntityD");
    assertThat(DummyEntityD.class.getAnnotation(Resource.class).path()).isEmpty();
    when(baseEntityCheckerService.filterBaseEntitySubClasses()).thenReturn(baseEntitySubClassesMap);

    handlerMappingConfig.configureResourceMapping();

    var entityName = baseEntitySubClassesMap.get(DummyEntityD.class);
    verify(controllerRegistererService).mapRestOperationsToController(
        argThat(path -> path.contains(entityName.toLowerCase(Locale.ROOT))), any(), any());

    verify(controllerRegistererService).registerRelatedPaths(
        argThat(path -> path.contains(entityName.toLowerCase(Locale.ROOT))), any(), any(), any());
  }




}