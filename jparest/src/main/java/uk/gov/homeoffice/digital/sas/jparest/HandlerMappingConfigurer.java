package uk.gov.homeoffice.digital.sas.jparest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Pageable;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo.BuilderConfiguration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;
import uk.gov.homeoffice.digital.sas.jparest.controller.ResourceApiController;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Discovers JPA entities annotated with {@link Resource}
 * and registers a {@link ResourceApiController} for them.
 */
@Component
public class HandlerMappingConfigurer extends RequestMappingHandlerMapping {

    private RequestMappingHandlerMapping requestMappingHandlerMapping;
    private BuilderConfiguration builderOptions;

    public HandlerMappingConfigurer() {
    }

    private final static Logger LOGGER = Logger.getLogger(HandlerMappingConfigurer.class.getName());

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    ApplicationContext context;

    @Autowired
    private ResourceEndpoint resourceEndpoint;

    @PostConstruct
    public void registerUserController() throws NoSuchMethodException, SecurityException, ClassNotFoundException {

        requestMappingHandlerMapping = context.getBean(RequestMappingHandlerMapping.class);
        builderOptions = new BuilderConfiguration();
        builderOptions.setPathMatcher(requestMappingHandlerMapping.getPathMatcher());
        builderOptions.setPatternParser(requestMappingHandlerMapping.getPatternParser());

        List<Class<?>> resourceTypes = resourceEndpoint.getResourceTypes();

        // TODO: Make the path configurable
        String apiRootPath = "/resources";

        LOGGER.fine("Searching for classes annotated as resources");
        for (EntityType<?> entityType : entityManager.getMetamodel().getEntities()) {
            Class<?> resource = entityType.getJavaType();

            // For each entity find the id field and build the request mapping path
            // and register the controller

            LOGGER.fine("Processing resource" + resource.getName());
            if (resource.isAnnotationPresent(Resource.class)) {
                Resource resourceAnnotation = resource.getAnnotation(Resource.class);
                String resourcePath = resourceAnnotation.path();
                if (!StringUtils.hasText(resourcePath)) {
                    resourcePath = entityType.getName().toLowerCase();
                }
                String path = apiRootPath + "/" + resourcePath;
                LOGGER.fine("root path for resource: " + path);

                // Added to endpoint resource types for documentation customiser
                resourceTypes.add(resource);

                // Create a controller for the resource
                LOGGER.fine("Creating controller");
                EntityUtils<?> entityUtils = new EntityUtils<>(resource, entityManager);
                ResourceApiController<?, ?> controller = new ResourceApiController<>(resource, entityManager, transactionManager, entityUtils);
                // Map the CRUD operations to the controllers methods

                LOGGER.fine("Registering common paths");
                register(resource, controller, "list", new Class<?>[]{SpelExpression.class, Pageable.class}, path, null, RequestMethod.GET);
                register(resource, controller, "get", new Class<?>[]{Object.class}, path + "/{id}", null, RequestMethod.GET);
                register(resource, controller, "create", new Class<?>[]{String.class}, path, null, RequestMethod.POST);
                register(resource, controller, "delete", new Class<?>[]{Object.class}, path + "/{id}", null, RequestMethod.DELETE);
                register(resource, controller, "update", new Class<?>[]{Object.class, String.class}, path + "/{id}", null, RequestMethod.PUT);

                resourceEndpoint.Add(resource, path, entityUtils.getIdFieldType());

                LOGGER.fine("Registering related paths");
                for (String relation : entityUtils.getRelatedResources()) {

                    Class<?> relatedType = entityUtils.getRelatedType(relation);
                    Class<?> relatedIdType = entityUtils.getRelatedIdType(relation);
                    resourceEndpoint.AddRelated(resource, relatedType, path + "/{id}/" + relation, relatedIdType);

                    LOGGER.fine("Registering related path: " + relation);
                    register(resource, controller, "getRelated", new Class<?>[]{Object.class, String.class, SpelExpression.class, Pageable.class}, path + "/{id}/{relation:" + Pattern.quote(relation) + "}", null, RequestMethod.GET);
                    register(resource, controller, "deleteRelated", new Class<?>[]{Object.class, String.class, Object[].class}, path + "/{id}/{relation:" + Pattern.quote(relation) + "}/{related_id}", null, RequestMethod.DELETE);
                    register(resource, controller, "addRelated", new Class<?>[]{Object.class, String.class, Object[].class}, path + "/{id}/{relation:" + Pattern.quote(relation) + "}/{related_id}", null, RequestMethod.PUT);
                }

                LOGGER.fine("All paths registered");
            }

        }


    }

    /**
     * Private help method that finds the specified method on the controller class and maps it to the
     * given path for the given request method.
     *
     * @param controller    The controller to register the mapping to
     * @param methodName    The method name to map to on the controller
     * @param methodArgs    The argument types expected by the specified method
     * @param path          The path to map
     * @param requestMethod The request method to map
     * @throws NoSuchMethodException
     */
    private void register(Class<?> resource, Object controller, String methodName, Class<?>[] methodArgs, String path, RequestCondition<?> condition, RequestMethod requestMethod) throws NoSuchMethodException {
        Method method = ResourceApiController.class.getDeclaredMethod(methodName, methodArgs);

        LOGGER.finest("Building RequestMappingInfo");
        RequestMappingInfo.Builder builder = RequestMappingInfo.paths(path).options(this.builderOptions)
                .methods(requestMethod)
                .produces(MediaType.APPLICATION_JSON_VALUE);

        if (condition != null) {
            builder = builder.customCondition(condition);
        }

        RequestMappingInfo requestMappingInfo = builder.build();

        LOGGER.finest("Registering mapping");
        requestMappingHandlerMapping.registerMapping(requestMappingInfo, controller, method);
        LOGGER.finest("Mapping registered");

    }

}