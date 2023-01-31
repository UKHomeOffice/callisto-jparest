package uk.gov.homeoffice.digital.sas.jparest.config;

import static uk.gov.homeoffice.digital.sas.jparest.utils.ConstantHelper.API_ROOT_PATH;
import static uk.gov.homeoffice.digital.sas.jparest.utils.ConstantHelper.PATH_DELIMITER;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import uk.gov.homeoffice.digital.sas.jparest.ResourceEndpoint;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;
import uk.gov.homeoffice.digital.sas.jparest.controller.ResourceApiController;
import uk.gov.homeoffice.digital.sas.jparest.factory.ResourceApiControllerFactory;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;
import uk.gov.homeoffice.digital.sas.jparest.service.BaseEntityCheckerService;
import uk.gov.homeoffice.digital.sas.jparest.service.ControllerRegistererService;


/**
 * Discovers JPA entities annotated with {@link Resource}
 * and registers a {@link ResourceApiController} for them.
 */
@Configuration
@AllArgsConstructor
public class HandlerMappingConfig<T extends BaseEntity> {

  private static final Logger LOGGER = Logger.getLogger(HandlerMappingConfig.class.getName());

  private final ResourceEndpoint resourceEndpoint;
  private final ResourceApiControllerFactory resourceApiControllerFactory;
  private final BaseEntityCheckerService baseEntityCheckerService;
  private final ControllerRegistererService controllerRegistererService;


  @PostConstruct
  public void configureResourceMapping() throws NoSuchMethodException, SecurityException {

    LOGGER.fine("Searching for classes annotated as resources");
    for (var entityClassEntry : baseEntityCheckerService.getBaseEntitySubClasses().entrySet()) {

      Class<T> resourceClass = (Class<T>) entityClassEntry.getKey();
      LOGGER.fine("Processing resource" + resourceClass.getName());

      String path = getPath(resourceClass, entityClassEntry.getValue());
      LOGGER.log(Level.FINE, "root path for resource: {0}", path);

      LOGGER.fine("Adding to endpoint resource type for documentation customiser");
      resourceEndpoint.addResourceType(resourceClass);

      LOGGER.fine("Creating controller");
      ResourceApiController<T> controller =
              resourceApiControllerFactory.getControllerBean(resourceClass);
      mapCrudOperationsToController(controller, resourceClass, path);
    }
  }

  private String getPath(Class<T> resourceClass, String entityName) {
    var resourceAnnotation = resourceClass.getAnnotation(Resource.class);
    String resourcePath = resourceAnnotation.path();
    if (!StringUtils.hasText(resourcePath)) {
      resourcePath = entityName.toLowerCase();
    }
    return API_ROOT_PATH + PATH_DELIMITER + resourcePath;
  }

  private void mapCrudOperationsToController(
      ResourceApiController<T> controller,
      Class<T> resourceClass,
      String path) throws NoSuchMethodException {

    Consumer<String> addResourcePathConsumer = pathArg -> resourceEndpoint.add(
        resourceClass, pathArg);
    controllerRegistererService.mapRestOperationsToController(
        path, controller, addResourcePathConsumer);

    BiConsumer<Class<? extends BaseEntity>, String> addRelatedResourcePathConsumer =
        (relatedClass, pathArg) -> resourceEndpoint.addRelated(
            resourceClass, relatedClass, pathArg);
    controllerRegistererService.registerRelatedPaths(
        path, resourceClass, controller, addRelatedResourcePathConsumer);

    LOGGER.fine("All paths registered");
  }


}