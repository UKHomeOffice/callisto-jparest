package uk.gov.homeoffice.digital.sas.jparest.service;

import static uk.gov.homeoffice.digital.sas.jparest.utils.ConstantHelper.URL_ID_PATH_PARAM;
import static uk.gov.homeoffice.digital.sas.jparest.utils.ConstantHelper.URL_RELATED_ID_PATH_PARAM;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import uk.gov.homeoffice.digital.sas.jparest.EntityUtils;
import uk.gov.homeoffice.digital.sas.jparest.controller.ResourceApiController;
import uk.gov.homeoffice.digital.sas.jparest.controller.enums.RequestParameter;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;


@Service
public class ControllerRegistererService {

  private final RequestMappingHandlerMapping requestMappingHandlerMapping;

  private static final Logger LOGGER = Logger.getLogger(
      ControllerRegistererService.class.getName());

  public ControllerRegistererService(RequestMappingHandlerMapping requestMappingHandlerMapping) {
    this.requestMappingHandlerMapping = requestMappingHandlerMapping;
  }

  public void mapRestOperationsToController(String path,
                                            ResourceApiController<?> controller,
                                            Consumer<String> resourceEndpointAdder)
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
    resourceEndpointAdder.accept(path);
  }

  public void registerRelatedPaths(
      String path,
      EntityUtils<? extends BaseEntity, ?> entityUtils,
      ResourceApiController<?> controller,
      BiConsumer<Class<? extends BaseEntity>, String> addRelatedResourceConsumer)
      throws NoSuchMethodException {

    LOGGER.fine("Registering related paths");
    for (String relation : entityUtils.getRelatedResources()) {

      Class<? extends BaseEntity> relatedType = entityUtils.getRelatedType(relation);
      addRelatedResourceConsumer.accept(relatedType, path + URL_ID_PATH_PARAM + "/" + relation);
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


  private String createIdAndRelationParams(String relation) {
    return String.format(URL_ID_PATH_PARAM.concat("/{%s:%s}"),
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

    Method method = ResourceApiController.class.getDeclaredMethod(methodName, methodArgs);

    LOGGER.finest("Building RequestMappingInfo");
    RequestMappingInfo.Builder builder = RequestMappingInfo.paths(path)
        .options(createBuilderOptions())
        .methods(requestMethod)
        .produces(MediaType.APPLICATION_JSON_VALUE);

    RequestMappingInfo requestMappingInfo = builder.build();

    LOGGER.finest("Registering mapping");
    requestMappingHandlerMapping.registerMapping(requestMappingInfo, controller, method);
    LOGGER.finest("Mapping registered");
  }

  private RequestMappingInfo.BuilderConfiguration createBuilderOptions() {
    RequestMappingInfo.BuilderConfiguration builderOptions =
        new RequestMappingInfo.BuilderConfiguration();
    builderOptions.setPathMatcher(requestMappingHandlerMapping.getPathMatcher());
    builderOptions.setPatternParser(requestMappingHandlerMapping.getPatternParser());
    return builderOptions;
  }

}
