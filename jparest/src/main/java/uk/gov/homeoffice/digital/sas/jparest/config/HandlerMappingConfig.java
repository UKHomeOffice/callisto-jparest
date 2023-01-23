package uk.gov.homeoffice.digital.sas.jparest.config;

import static uk.gov.homeoffice.digital.sas.jparest.utils.ConstantHelper.API_ROOT_PATH;
import static uk.gov.homeoffice.digital.sas.jparest.utils.ConstantHelper.PATH_DELIMITER;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import uk.gov.homeoffice.digital.sas.jparest.EntityUtils;
import uk.gov.homeoffice.digital.sas.jparest.ResourceEndpoint;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;
import uk.gov.homeoffice.digital.sas.jparest.controller.ResourceApiController;
import uk.gov.homeoffice.digital.sas.jparest.controller.ResourceApiControllerFactory;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;
import uk.gov.homeoffice.digital.sas.jparest.repository.TenantRepositoryImpl;
import uk.gov.homeoffice.digital.sas.jparest.service.BaseEntityCheckerService;
import uk.gov.homeoffice.digital.sas.jparest.service.ControllerRegistererService;
import uk.gov.homeoffice.digital.sas.jparest.service.ResourceApiService;
import uk.gov.homeoffice.digital.sas.jparest.service.ResourceApiServiceFactory;


/**
 * Discovers JPA entities annotated with {@link Resource}
 * and registers a {@link ResourceApiController} for them.
 */
@Configuration
public class HandlerMappingConfig {

  private static final Logger LOGGER = Logger.getLogger(HandlerMappingConfig.class.getName());

  private final EntityManager entityManager;
  private final ResourceEndpoint resourceEndpoint;
  private final ResourceApiServiceFactory resourceApiServiceFactory;
  private final ResourceApiControllerFactory resourceApiControllerFactory;
  private final BaseEntityCheckerService baseEntityCheckerService;
  private final ControllerRegistererService controllerRegistererService;



  public HandlerMappingConfig(
      EntityManager entityManager,
      ResourceApiServiceFactory resourceApiServiceFactory,
      ResourceEndpoint resourceEndpoint,
      ResourceApiControllerFactory resourceApiControllerFactory,
      BaseEntityCheckerService baseEntityCheckerService,
      ControllerRegistererService controllerRegistererService) {
    this.entityManager = entityManager;
    this.resourceApiServiceFactory = resourceApiServiceFactory;
    this.resourceEndpoint = resourceEndpoint;
    this.resourceApiControllerFactory = resourceApiControllerFactory;
    this.baseEntityCheckerService = baseEntityCheckerService;
    this.controllerRegistererService = controllerRegistererService;
  }

  @PostConstruct
  public <T extends BaseEntity> void registerUserController()
      throws NoSuchMethodException, SecurityException {

    LOGGER.fine("Searching for classes annotated as resources");
    List<Class<?>> resourceTypes = resourceEndpoint.getResourceTypes();

    Map<Class<?>, EntityType<?>> baseEntitySubClassesMap =
        baseEntityCheckerService.filterBaseEntitySubClasses();


    // find the id field , build the request mapping path and register the controller
    for (var entityClassEntry : baseEntitySubClassesMap.entrySet()) {

      Class<T> resourceClass = (Class<T>) entityClassEntry.getKey();
      LOGGER.fine("Processing resource" + resourceClass.getName());
      var resourceAnnotation = resourceClass.getAnnotation(Resource.class);
      String resourcePath = resourceAnnotation.path();
      if (!StringUtils.hasText(resourcePath)) {
        resourcePath = entityClassEntry.getValue().getName().toLowerCase();
      }
      String path = API_ROOT_PATH + PATH_DELIMITER + resourcePath;
      LOGGER.log(Level.FINE, "root path for resource: {0}", path);

      // Added to endpoint resource types for documentation customiser
      //TODO: change to add through a new ResourceEndpoint service method
      resourceTypes.add(resourceClass);

      var entityUtils = new EntityUtils<>(resourceClass,
          baseEntityCheckerService.isBaseEntitySubclass(baseEntitySubClassesMap));
      ResourceApiController<T> controller = createController(resourceClass, entityUtils);
      mapCrudOperationsToController(controller, entityUtils, resourceClass, path);
    }
  }

  private <T extends BaseEntity>  ResourceApiController<T> createController(
      Class<T> resourceClass,
      EntityUtils<T, ? extends BaseEntity> entityUtils) {

    LOGGER.fine("Creating controller");
    ResourceApiService<T> service = resourceApiServiceFactory.getBean(resourceClass, entityUtils,
        new TenantRepositoryImpl<>(resourceClass, entityManager));
    return resourceApiControllerFactory.getBean(resourceClass, service);
  }

  private <T extends BaseEntity> void mapCrudOperationsToController(
      ResourceApiController<T> controller,
      EntityUtils<T, ? extends BaseEntity> entityUtils,
      Class<T> resourceClass,
      String path) throws NoSuchMethodException {

    Consumer<String> addResourceConsumer = pathArg -> resourceEndpoint.add(
        resourceClass, pathArg);
    controllerRegistererService.mapRestOperationsToController(
        path, controller, addResourceConsumer);

    BiConsumer<Class<? extends BaseEntity>, String> addRelatedResourceConsumer =
        (relatedClass, pathArg) -> resourceEndpoint.addRelated(
            resourceClass, relatedClass, pathArg);
    controllerRegistererService.registerRelatedPaths(
        path, entityUtils, controller, addRelatedResourceConsumer);

    LOGGER.fine("All paths registered");
  }


}