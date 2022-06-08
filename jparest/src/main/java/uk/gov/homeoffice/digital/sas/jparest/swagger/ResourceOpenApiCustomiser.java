package uk.gov.homeoffice.digital.sas.jparest.swagger;


import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.SpringDocAnnotationsUtils;
import org.springdoc.core.converters.models.Pageable;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.homeoffice.digital.sas.jparest.ResourceEndpoint;

import java.util.Map.Entry;

import static uk.gov.homeoffice.digital.sas.jparest.utils.ConstantHelper.URL_ID_PATH_PARAM;
import static uk.gov.homeoffice.digital.sas.jparest.utils.ConstantHelper.URL_RELATED_ID_PATH_PARAM;

/**
 * Extends the OpenApi model to include the endpoints added by the resource
 * annotation
 */
@Component
public class ResourceOpenApiCustomiser implements OpenApiCustomiser {


    public static final int DEFAULT_PAGE_SIZE = 10;
    private final ResourceEndpoint endpoint;
    private final PathItemCreator pathItemCreator;

    @Autowired
    public ResourceOpenApiCustomiser(ResourceEndpoint endpoint, PathItemCreator pathItemCreator) {
        this.endpoint = endpoint;
        this.pathItemCreator = pathItemCreator;
    }



    /**
     * Customises the generated openApi for the endpoints exposed by the
     * ResourceApiController
     */
    public void customise(OpenAPI openApi) {

        var components = openApi.getComponents();
        var apiResponseSchema = registerSchema(components);
        setResponseSchema(apiResponseSchema, components);


        // Iterate the ResourceEndpoint descriptors to
        // generate documentation for all of the registered endpoints
        for (Entry<Class<?>, ResourceEndpoint.RootDescriptor> element : endpoint.getDescriptors().entrySet()) {

            var clazz = element.getKey();
            var rootDescriptor = element.getValue();

            // Group all of the resource endpoints together
            var tag = clazz.getSimpleName();

            // Create documentation for the entity
            setParentResourcePaths(openApi, rootDescriptor, clazz, tag);

            // Create documentation for relations in the entity
            for (Entry<Class<?>, ResourceEndpoint.Descriptor> relatedElement : rootDescriptor.getRelations()
                    .entrySet()) {

                var relatedClazz = relatedElement.getKey();
                var relatedDescriptor = relatedElement.getValue();
                setRelatedResourcePaths(openApi, rootDescriptor, relatedDescriptor, relatedClazz, tag);
            }
        }
    }



    /**
     * Ensures the ApiResponse schema is registered along with the metadata schema
     */
    private static Schema<?> registerSchema(Components components) {
        var apiResponseSchema = ensureSchema(components, "ApiResponse", uk.gov.homeoffice.digital.sas.jparest.web.ApiResponse.class);
        ensureSchema(components, "Metadata", uk.gov.homeoffice.digital.sas.jparest.web.ApiResponse.Metadata.class);
        var pageableSchema = ensureSchema(components, "Pageable", Pageable.class);
        var value = new Pageable(0, DEFAULT_PAGE_SIZE, null);
        pageableSchema.setExample(value);
        return apiResponseSchema;
    }

    /**
     * Gets the schema for the response object and then extends the items
     * property to be one of the entities exposed by the controller
     */
    private void setResponseSchema(Schema<?> apiResponseSchema, Components components) {
        var composedSchema = new ComposedSchema();
        for (Class<?> resource : endpoint.getResourceTypes()) {
            composedSchema.addOneOfItem(
                    SpringDocAnnotationsUtils.extractSchema(components, resource, null, null));
        }
        var arraySchema = (ArraySchema) apiResponseSchema.getProperties()
                .get("items");
        arraySchema.setItems(composedSchema);
    }


    private void setParentResourcePaths(OpenAPI openApi, ResourceEndpoint.RootDescriptor rootDescriptor, Class<?> clazz, String tag) {
        var resourceRootPath = pathItemCreator.createRootPath(tag, clazz);
        openApi.path(rootDescriptor.getPath(), resourceRootPath);
        var resourceItemPath = pathItemCreator.createItemPath(tag, clazz, rootDescriptor.getIdFieldType());
        openApi.path(rootDescriptor.getPath() + URL_ID_PATH_PARAM, resourceItemPath);
    }

    private void setRelatedResourcePaths(OpenAPI openApi,
                                         ResourceEndpoint.RootDescriptor rootDescriptor,
                                         ResourceEndpoint.Descriptor relatedDescriptor,
                                         Class<?> relatedClazz, String tag) {

        var relatedRootPath = pathItemCreator.createRelatedRootPath(tag, relatedClazz, rootDescriptor.getIdFieldType());
        openApi.path(relatedDescriptor.getPath(), relatedRootPath);
        var relatedItemPath = pathItemCreator.createRelatedItemPath(tag, rootDescriptor.getIdFieldType(),
                relatedDescriptor.getIdFieldType());
        openApi.path(relatedDescriptor.getPath() + URL_RELATED_ID_PATH_PARAM, relatedItemPath);
    }



    /**
     * Util function to get or create schema
     */
    private static Schema<?> ensureSchema(Components components, String schemaName,
                                          Class<?> clazz) {
        var schemas = components.getSchemas();
        var schema = schemas == null ? null : schemas.get(schemaName);
        if (schema == null) {
            schema = ModelConverters.getInstance()
                    .read(new AnnotatedType(clazz)
                            .resolveAsRef(false))
                    .get(schemaName);
            components.addSchemas(schemaName, schema);
        }
        return schema;
    }


}
