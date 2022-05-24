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
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo.BuilderConfiguration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;
import uk.gov.homeoffice.digital.sas.jparest.controller.ResourceApiController;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static uk.gov.homeoffice.digital.sas.jparest.utils.ConstantHelper.*;

/**
 * Discovers JPA entities annotated with {@link Resource}
 * and registers a {@link ResourceApiController} for them.
 */
@Component
public class HandlerMappingConfigurer extends RequestMappingHandlerMapping {

    private static final Logger LOGGER = Logger.getLogger(HandlerMappingConfigurer.class.getName());

    private final EntityManager entityManager;
    private final PlatformTransactionManager transactionManager;
    private final ResourceEndpoint resourceEndpoint;
    private final ApplicationContext context;
    private RequestMappingHandlerMapping requestMappingHandlerMapping;
    private BuilderConfiguration builderOptions;

    @Autowired
    public HandlerMappingConfigurer(
            EntityManager entityManager,
            PlatformTransactionManager transactionManager,
            ApplicationContext context,
            ResourceEndpoint resourceEndpoint) {
        this.entityManager = entityManager;
        this.transactionManager = transactionManager;
        this.context = context;
        this.resourceEndpoint = resourceEndpoint;
    }

    @PostConstruct
    public void registerUserController() throws NoSuchMethodException, SecurityException {
        requestMappingHandlerMapping = context.getBean(RequestMappingHandlerMapping.class);

        createBuilderOptions();

        LOGGER.fine("Searching for classes annotated as resources");
        List<Class<?>> resourceTypes = resourceEndpoint.getResourceTypes();
        Set<EntityType<?>> resourceEntityTypes =
                entityManager.getMetamodel().getEntities()
                        .stream()
                        .filter(et -> et.getJavaType().isAnnotationPresent(Resource.class))
                        .collect(Collectors.toSet());

        // find the id field , build the request mapping path and register the controller
        for (EntityType<?> entityType : resourceEntityTypes) {
            Class<? extends BaseEntity> resource = (Class<? extends BaseEntity>) entityType.getJavaType();
            LOGGER.fine("Processing resource" + resource.getName());
            var resourceAnnotation = resource.getAnnotation(Resource.class);
            String resourcePath = resourceAnnotation.path();
            if (!StringUtils.hasText(resourcePath)) {
                resourcePath = entityType.getName().toLowerCase();
            }
            String path = API_ROOT_PATH + PATH_DELIMITER + resourcePath;
            LOGGER.log(Level.FINE, "root path for resource: {0}", path);

            // Added to endpoint resource types for documentation customiser
            resourceTypes.add(resource);

            // Create a controller for the resource
            LOGGER.fine("Creating controller");
            EntityUtils<?> entityUtils = new EntityUtils<>(resource, entityManager);
            ResourceApiController<?, ?> controller = new ResourceApiController<>(
                    resource, entityManager,
                    transactionManager, entityUtils);

            // Map the CRUD operations to the controllers methods
            mapRestOperationsToController(resource, path, entityUtils, controller);

            LOGGER.fine("Registering related paths");
            registerRelatedPaths(resource, path, entityUtils, controller);

            LOGGER.fine("All paths registered");
        }
    }

    private void mapRestOperationsToController(
            Class<?> resource, String path, EntityUtils<?> entityUtils,
            ResourceApiController<?, ?> controller) throws NoSuchMethodException {
        LOGGER.fine("Registering common paths");
        register(controller, "list", new Class<?>[]{SpelExpression.class, Pageable.class, UUID.class},
                path, RequestMethod.GET);
        register(controller, "get", new Class<?>[]{Object.class, UUID.class},
                path + URL_ID_PATH_PARAM, RequestMethod.GET);
        register(controller, "create", new Class<?>[]{String.class, UUID.class},
                path, RequestMethod.POST);
        register(controller, "delete", new Class<?>[]{Object.class, UUID.class},
                path + URL_ID_PATH_PARAM, RequestMethod.DELETE);
        register(controller, "update", new Class<?>[]{Object.class, String.class, UUID.class},
                path + URL_ID_PATH_PARAM, RequestMethod.PUT);
        resourceEndpoint.add(resource, path, entityUtils.getIdFieldType());
    }

    private void registerRelatedPaths(
            Class<?> resource, String path, EntityUtils<?> entityUtils,
            ResourceApiController<?, ?> controller) throws NoSuchMethodException {
        for (String relation : entityUtils.getRelatedResources()) {
            Class<?> relatedType = entityUtils.getRelatedType(relation);
            Class<?> relatedIdType = entityUtils.getRelatedIdType(relation);
            resourceEndpoint.addRelated(resource, relatedType,
                    path + URL_ID_PATH_PARAM + "/" + relation, relatedIdType);
            LOGGER.log(Level.FINE, "Registering related path: : {0}", relation);
            register(controller, "getRelated",
                    new Class<?>[]{Object.class, String.class, SpelExpression.class, Pageable.class, UUID.class},
                    path + createIdAndRelationParams(relation), RequestMethod.GET);
            register(controller, "deleteRelated", new Class<?>[]{Object.class, String.class, Object[].class, UUID.class},
                    path + createIdAndRelationParams(relation) + URL_RELATED_ID_PATH_PARAM,
                    RequestMethod.DELETE);
            register(controller, "addRelated", new Class<?>[]{Object.class, String.class, Object[].class, UUID.class},
                    path + createIdAndRelationParams(relation) + URL_RELATED_ID_PATH_PARAM, RequestMethod.PUT);
        }
    }

    private void createBuilderOptions() {
        builderOptions = new BuilderConfiguration();
        builderOptions.setPathMatcher(requestMappingHandlerMapping.getPathMatcher());
        builderOptions.setPatternParser(requestMappingHandlerMapping.getPatternParser());
    }

    private static String createIdAndRelationParams(String relation) {
        return URL_ID_PATH_PARAM + "/{relation:" + Pattern.quote(relation) + "}";
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
     * @throws NoSuchMethodException
     */
    private void register(Object controller, String methodName, Class<?>[] methodArgs, String path,
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