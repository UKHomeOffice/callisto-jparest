package uk.gov.homeoffice.digital.sas.jparest.config;

import static uk.gov.homeoffice.digital.sas.jparest.utils.ConstantHelper.API_ROOT_PATH;
import static uk.gov.homeoffice.digital.sas.jparest.utils.ConstantHelper.PATH_DELIMITER;
import static uk.gov.homeoffice.digital.sas.jparest.utils.ConstantHelper.URL_ID_PATH_PARAM;
import static uk.gov.homeoffice.digital.sas.jparest.utils.ConstantHelper.URL_RELATED_ID_PATH_PARAM;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo.BuilderConfiguration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import uk.gov.homeoffice.digital.sas.jparest.EntityUtils;
import uk.gov.homeoffice.digital.sas.jparest.ResourceEndpoint;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;
import uk.gov.homeoffice.digital.sas.jparest.controller.ResourceApiController;
import uk.gov.homeoffice.digital.sas.jparest.controller.enums.RequestParameter;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;
import uk.gov.homeoffice.digital.sas.jparest.validation.EntityValidator;

/**
 * Discovers JPA entities annotated with {@link Resource}
 * and registers a {@link ResourceApiController} for them.
 */
@Configuration
public class HandlerMappingConfig {

  private static final Logger LOGGER = Logger.getLogger(HandlerMappingConfig.class.getName());

  private final EntityManager entityManager;
  private final PlatformTransactionManager transactionManager;
  private final ResourceEndpoint resourceEndpoint;
  private final ApplicationContext context;
  private RequestMappingHandlerMapping requestMappingHandlerMapping;
  private BuilderConfiguration builderOptions;
  private final EntityValidator entityValidator;
  private final ObjectMapper objectMapper;

  public HandlerMappingConfig(
      EntityManager entityManager,
      PlatformTransactionManager transactionManager,
      ApplicationContext context,
      ResourceEndpoint resourceEndpoint,
      EntityValidator entityValidator,
      ObjectMapper objectMapper) {
    this.entityManager = entityManager;
    this.transactionManager = transactionManager;
    this.context = context;
    this.resourceEndpoint = resourceEndpoint;
    this.entityValidator = entityValidator;
    this.objectMapper = objectMapper;
  }

  @PostConstruct
  public void registerUserController() throws NoSuchMethodException, SecurityException {
    requestMappingHandlerMapping = context.getBean(RequestMappingHandlerMapping.class);

    createBuilderOptions();

    LOGGER.fine("Searching for classes annotated as resources");
    List<Class<?>> resourceTypes = resourceEndpoint.getResourceTypes();

    Map<Class<?>, EntityType<?>> baseEntitySubClassesMap =
        entityManager.getMetamodel().getEntities()
            .stream()
            .filter(entityType -> entityType.getJavaType().isAnnotationPresent(Resource.class)
                && classHasBaseEntityParent(entityType.getJavaType()))
            .collect(Collectors.toMap(EntityType::getJavaType, Function.identity()));

    Predicate<Class<?>> isBaseEntitySubclass = baseEntitySubClassesMap::containsKey;


    // find the id field , build the request mapping path and register the controller
    for (var entityClassEntry : baseEntitySubClassesMap.entrySet()) {

      Class<? extends BaseEntity> resource = (
          Class<? extends BaseEntity>) entityClassEntry.getKey();
      LOGGER.fine("Processing resource" + resource.getName());
      var resourceAnnotation = resource.getAnnotation(Resource.class);
      String resourcePath = resourceAnnotation.path();
      if (!StringUtils.hasText(resourcePath)) {
        resourcePath = entityClassEntry.getValue().getName().toLowerCase();
      }
      String path = API_ROOT_PATH + PATH_DELIMITER + resourcePath;
      LOGGER.log(Level.FINE, "root path for resource: {0}", path);

      // Added to endpoint resource types for documentation customiser
      resourceTypes.add(resource);

      // Create a controller for the resource
      LOGGER.fine("Creating controller");
      EntityUtils<?, ?> entityUtils = new EntityUtils<>(resource, isBaseEntitySubclass);
      ResourceApiController<?> controller = new ResourceApiController<>(
          resource, entityManager,
          transactionManager, entityUtils, entityValidator, objectMapper);

      // Map the CRUD operations to the controllers methods
      mapRestOperationsToController(resource, path, controller);

      LOGGER.fine("Registering related paths");
      registerRelatedPaths(resource, path, entityUtils, controller);

      LOGGER.fine("All paths registered");
    }
  }

  private boolean classHasBaseEntityParent(Class<?> childClass) {
    var superType = childClass.getSuperclass();
    while (!superType.equals(Object.class)) {
      if (superType.equals(BaseEntity.class)) {
        return true;
      }
      superType = superType.getSuperclass();
    }
    return false;
  }

  private void mapRestOperationsToController(Class<?> resource,
                                             String path,
                                             ResourceApiController<?> controller)
      throws NoSuchMethodException {

    LOGGER.fine("Registering common paths");

    register(controller, "list",
        getControllerMethodArgs(RequestParameter.TENANT_ID,
            RequestParameter.PAGEABLE, RequestParameter.FILTER),
        path, RequestMethod.GET);
    register(controller, "get",
        getControllerMethodArgs(RequestParameter.TENANT_ID, RequestParameter.ID),
        path + URL_ID_PATH_PARAM, RequestMethod.GET);
    register(controller, "create",
        getControllerMethodArgs(RequestParameter.TENANT_ID, RequestParameter.BODY),
        path, RequestMethod.POST);
    register(controller, "delete",
        getControllerMethodArgs(RequestParameter.TENANT_ID, RequestParameter.ID),
        path + URL_ID_PATH_PARAM, RequestMethod.DELETE);
    register(controller, "update",
        getControllerMethodArgs(RequestParameter.TENANT_ID,
            RequestParameter.ID, RequestParameter.BODY),
        path + URL_ID_PATH_PARAM, RequestMethod.PUT);
    resourceEndpoint.add(resource, path);
  }

  private void registerRelatedPaths(Class<? extends BaseEntity> resource,
                                    String path,
                                    EntityUtils<? extends BaseEntity, ?> entityUtils,
                                    ResourceApiController<?> controller)
      throws NoSuchMethodException {

    for (String relation : entityUtils.getRelatedResources()) {

      Class<? extends BaseEntity> relatedType = entityUtils.getRelatedType(relation);
      resourceEndpoint.addRelated(resource, relatedType,
          path + URL_ID_PATH_PARAM + "/" + relation);
      LOGGER.log(Level.FINE, "Registering related path: : {0}", relation);

      register(controller, "getRelated",
          getControllerMethodArgs(
              RequestParameter.TENANT_ID,
              RequestParameter.ID,
              RequestParameter.RELATION,
              RequestParameter.FILTER,
              RequestParameter.PAGEABLE),
          path + createIdAndRelationParams(relation), RequestMethod.GET);

      register(controller, "deleteRelated", getControllerMethodArgs(
              RequestParameter.TENANT_ID, RequestParameter.ID, RequestParameter.RELATION,
              RequestParameter.RELATED_IDS),
          path + createIdAndRelationParams(relation) + URL_RELATED_ID_PATH_PARAM,
          RequestMethod.DELETE);

      register(controller, "addRelated", getControllerMethodArgs(
              RequestParameter.TENANT_ID, RequestParameter.ID, RequestParameter.RELATION,
              RequestParameter.RELATED_IDS),
          path + createIdAndRelationParams(relation)
              + URL_RELATED_ID_PATH_PARAM, RequestMethod.PUT);
    }
  }

  private Class<?>[] getControllerMethodArgs(RequestParameter... requestParameters) {
    return Stream.of(requestParameters)
        .sorted(Comparator.comparing(RequestParameter::getOrder))
        .map(RequestParameter::getParamDataType).toArray(Class<?>[]::new);
  }

  private void createBuilderOptions() {
    builderOptions = new BuilderConfiguration();
    builderOptions.setPathMatcher(requestMappingHandlerMapping.getPathMatcher());
    builderOptions.setPatternParser(requestMappingHandlerMapping.getPatternParser());
  }

  private static String createIdAndRelationParams(String relation) {
    return String.format(URL_ID_PATH_PARAM + "/{%s:%s}",
        RequestParameter.RELATION.getParamName(), Pattern.quote(relation));
  }

  /**
   * Private help method that finds the specified method on the controller class
   * and maps it to the
   * given path for the given request method.
   *
   * @param controller    The controller to register the mapping to
   * @param methodName    The method name to map to on the controller
   * @param methodArgs    The argument types expected by the specified method
   * @param path          The path to map
   * @param requestMethod The request method to map
   * @throws NoSuchMethodException when declared method cannot be found
   */
  private void register(Object controller,
                        String methodName,
                        Class<?>[] methodArgs,
                        String path,
                        RequestMethod requestMethod) throws NoSuchMethodException {

    var method = ResourceApiController.class.getDeclaredMethod(methodName, methodArgs);

    LOGGER.finest("Building RequestMappingInfo");
    var builder = RequestMappingInfo.paths(path).options(this.builderOptions)
        .methods(requestMethod)
        .produces(MediaType.APPLICATION_JSON_VALUE);

    var requestMappingInfo = builder.build();

    LOGGER.finest("Registering mapping");
    requestMappingHandlerMapping.registerMapping(requestMappingInfo, controller, method);
    LOGGER.finest("Mapping registered");
  }

}