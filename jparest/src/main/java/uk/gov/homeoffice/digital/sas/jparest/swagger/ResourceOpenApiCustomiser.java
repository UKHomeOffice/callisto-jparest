package uk.gov.homeoffice.digital.sas.jparest.swagger;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import org.springdoc.core.SpringDocAnnotationsUtils;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;
import uk.gov.homeoffice.digital.sas.jparest.ResourceEndpoint;
import uk.gov.homeoffice.digital.sas.jparest.web.ApiResponse;

/**
 * Extends the OpenApi model to include the endpoints added by the resource
 * annotation
 */
public class ResourceOpenApiCustomiser implements OpenApiCustomiser {

    // private final static Logger LOGGER =
    // Logger.getLogger(ResourceOpenApiCustomiser.class.getName());

    @Autowired
    private ResourceEndpoint endpoint;

    /**
     * Customises the generated openApi for the endpoints exposed by the
     * ResourceApiController
     */
    public void customise(OpenAPI openApi) {

        Components components = openApi.getComponents();

        // Get the schema for the response object and then extend the items
        // property to be one of the entities exposed by the controller
        ComposedSchema composedSchema = new ComposedSchema();
        for (Class<?> resource : endpoint.getResourceTypes()) {
            composedSchema.addOneOfItem(
                    SpringDocAnnotationsUtils.extractSchema(components, resource, null, null));
        }
        ArraySchema arraySchema = (ArraySchema) components.getSchemas().get("ApiResponseObject").getProperties()
                .get("items");
        arraySchema.setItems(composedSchema);

        // Iterate the paths and for any that map to the ResourceApiController
        // improve the schema to include the resource type
        // in the response object and the request body
        Map<String, Class<?>> endpoints = endpoint.getEndpoints();
        for (Entry<String, PathItem> o : openApi.getPaths().entrySet()) {
            String key = o.getKey();
            PathItem pathItem = o.getValue();

            // Check if the path is from the ResourceApiController
            // and if so group the paths under the resource
            if (endpoints.containsKey(key)) {

                // Get a strongly typed schema for the response
                Class<?> resource = endpoints.get(key);
                Schema<?> responseSchema = getTypedApiResponseSchema(resource);

                // For get set the tag and update the response schema
                Operation operation = pathItem.getGet();
                if (operation != null) {
                    operation.setTags(Arrays.asList(resource.getSimpleName()));
                    operation.getResponses().get("200").getContent().get("*/*").setSchema(responseSchema);
                }

                // For post set the tag and update the request body and response schema
                operation = pathItem.getPost();
                if (operation != null) {
                    operation.setTags(Arrays.asList(resource.getSimpleName()));
                    Schema<?> schema = SpringDocAnnotationsUtils.extractSchema(components, resource, null,
                            null);
                    operation.getRequestBody().getContent().get("application/json").setSchema(schema);
                    operation.getResponses().get("200").getContent().get("*/*").setSchema(responseSchema);
                }

                // For delete set the tag
                operation = pathItem.getDelete();
                if (operation != null) {
                    operation.setTags(Arrays.asList(resource.getSimpleName()));
                }

                // For put set the tag and update the request body and response schema
                operation = pathItem.getPut();
                if (operation != null) {
                    operation.setTags(Arrays.asList(resource.getSimpleName()));
                    Schema<?> schema = SpringDocAnnotationsUtils.extractSchema(components, resource, null,
                            null);
                    operation.getRequestBody().getContent().get("application/json").setSchema(schema);
                    operation.getResponses().get("200").getContent().get("*/*").setSchema(responseSchema);
                }
            }
        }

    }

    /**
     * Returns a schema for the ApiResponse class with the items
     * property containing a schema for the given class
     * 
     * @param clazz The type to generate a schema for
     * @return Schema for the ApiResponse
     */
    private Schema<?> getTypedApiResponseSchema(Class<?> clazz) {

        // Generate a schema for the ApiResponse
        Schema<?> schema = ModelConverters.getInstance()
                .read(new AnnotatedType(ApiResponse.class).resolveAsRef(false)).get("ApiResponse");

        // Get the schema for the given class
        Components newComponents = new Components();
        Schema<?> clazzSchema = SpringDocAnnotationsUtils.extractSchema(newComponents, clazz, null, null);

        // Set the schema of the items property to the class schema
        ArraySchema arraySchema = (ArraySchema) schema.getProperties().get("items");
        arraySchema.setItems(clazzSchema);

        return schema;

    }
}
