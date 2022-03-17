package uk.gov.homeoffice.digital.sas.jparest.swagger;



import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.util.AnnotationsUtils;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.SpringDocAnnotationsUtils;
import org.springdoc.core.converters.models.Pageable;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.homeoffice.digital.sas.jparest.ResourceEndpoint;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;

import java.util.Map.Entry;

/**
 * Extends the OpenApi model to include the endpoints added by the resource
 * annotation
 */
public class ResourceOpenApiCustomiser implements OpenApiCustomiser {

    @Autowired
    private ResourceEndpoint endpoint;

    private static ApiResponse emptyResponse = emptyResponse();
    private static Parameter pageableParameter = pageableParameter();

    /**
     * Customises the generated openApi for the endpoints exposed by the
     * ResourceApiController
     */
    public void customise(OpenAPI openApi) {
        var components = openApi.getComponents();

        // Ensure the ApiResponse schema is registered
        // along with the metadata schema
        Schema<?> apiResponseSchema = ensureSchema(components, "ApiResponse", uk.gov.homeoffice.digital.sas.jparest.web.ApiResponse.class);
        ensureSchema(components, "Metadata", uk.gov.homeoffice.digital.sas.jparest.web.ApiResponse.Metadata.class);
        Schema<?> pageableSchema = ensureSchema(components, "Pageable", Pageable.class);
        var value = new Pageable(0,10, null);
        pageableSchema.setExample(value);

        // Get the schema for the response object and then extend the items
        // property to be one of the entities exposed by the controller
        var composedSchema = new ComposedSchema();
        for (Class<?> resource : endpoint.getResourceTypes()) {
            composedSchema.addOneOfItem(
                    SpringDocAnnotationsUtils.extractSchema(components, resource, null, null));
        }
        var arraySchema = (ArraySchema) apiResponseSchema.getProperties()
                .get("items");
        arraySchema.setItems(composedSchema);

        // Iterate the ResourceEndpoint descriptors to
        // generate documentation for all of the registered endpoints
        for (Entry<Class<?>, ResourceEndpoint.RootDescriptor> element : endpoint.getDescriptors().entrySet()) {
            Class<?> clazz = element.getKey();
            var rootDescriptor = element.getValue();

            // Group all of the resource endpoints together
            String tag = clazz.getSimpleName();

            // Create documentation for the entity
            PathItem resourceRootPath = createRootPath(tag, clazz);
            openApi.path(rootDescriptor.getPath(), resourceRootPath);
            PathItem resourceItemPath = createItemPath(tag, clazz, rootDescriptor.getIdFieldType());
            openApi.path(rootDescriptor.getPath() + "/{id}", resourceItemPath);

            // Create documentation for relations in the entity
            for (Entry<Class<?>, ResourceEndpoint.Descriptor> relatedElement : rootDescriptor.getRelations()
                    .entrySet()) {

                Class<?> relatedClazz = relatedElement.getKey();
                var relatedDescriptor = relatedElement.getValue();

                PathItem relatedRootPath = createRelatedRootPath(tag, relatedClazz, rootDescriptor.getIdFieldType());
                openApi.path(relatedDescriptor.getPath(), relatedRootPath);
                PathItem relatedItemPath = createRelatedItemPath(tag, rootDescriptor.getIdFieldType(),
                        relatedDescriptor.getIdFieldType());
                openApi.path(relatedDescriptor.getPath() + "/{related_id}", relatedItemPath);

            }
        }
    }

    /**
     * Creates documentation for the endpoints of the resource
     * covers get many and post
     *
     * @param tag   The tag to group the endpoints together. Expected to be the
     *              simplename of the resource
     * @param clazz The class representing the resource exposed by the endpoint
     * @return PathItem documenting the GET many and POST endpoints
     */
    private PathItem createRootPath(String tag, Class<?> clazz) {

        var pi = new PathItem();

        ApiResponse response = getResourceResponse(clazz);
        ApiResponses responses = new ApiResponses().addApiResponse("200", response);


        var get = new Operation();
        get.addParametersItem(pageableParameter);
        get.addParametersItem(getFilterParameter(clazz));
        get.setResponses(responses);
        get.addTagsItem(tag);
        pi.get(get);

        var post = new Operation();
        post.setResponses(responses);
        post.addTagsItem(tag);
        pi.post(post);

        return pi;
    }

    /**
     * Creates documentation for the endpoints of the resource
     * covers get and put (update) individual resource
     *
     * @param tag     The tag to group the endpoints together. Expected to be the
     *                simplename of the resource
     * @param clazz   The class representing the resource exposed by the endpoint
     * @param idClazz The type of the identifier for the specified resource
     * @return PathItem documenting the GET one and PUT endpoints
     */
    private PathItem createItemPath(String tag, Class<?> clazz, Class<?> idClazz) {

        var pi = new PathItem();

        ApiResponse response = getResourceResponse(clazz);
        ApiResponses responses = new ApiResponses().addApiResponse("200", response);
        var get = new Operation();
        get.setResponses(responses);
        get.addTagsItem(tag);
        pi.get(get);

        var idParameter = getParameter(idClazz, "path", "id");
        get.addParametersItem(idParameter);
        var put = new Operation();
        put.setResponses(responses);
        put.addTagsItem(tag);
        pi.put(put);
        var delete = new Operation();

        ApiResponses deleteResponses = new ApiResponses().addApiResponse("200", emptyResponse);
        delete.addParametersItem(idParameter);
        delete.setResponses(deleteResponses);
        delete.addTagsItem(tag);
        pi.delete(delete);

        return pi;
    }

    /**
     * Creates documentation for the endpoints of the resource
     * covers get related resources for an individual resource
     *
     * @param tag     The tag to group the endpoints together. Expected to be the
     *                simplename of the parent resource
     * @param clazz   The class representing the related resource exposed by the
     *                endpoint
     * @param idClazz The type of the identifier for the parent resource
     * @return PathItem documenting the GET many related items endpoint
     */
    private PathItem createRelatedRootPath(String tag, Class<?> clazz, Class<?> idClazz) {

        var pi = new PathItem();
        ApiResponse response = getResourceResponse(clazz);
        ApiResponses responses = new ApiResponses().addApiResponse("200", response);
        var idParameter = getParameter(idClazz, "path", "id");

        var get = new Operation();
        get.addParametersItem(idParameter);
        get.addParametersItem(pageableParameter);
        get.addParametersItem(getFilterParameter(clazz));
        get.setResponses(responses);
        get.addTagsItem(tag);
        pi.get(get);
        return pi;
    }

    /**
     * Creates documentation for the endpoints of the resource
     * covers delete and put related resources for an individual resource
     *
     * @param tag            The tag to group the endpoints together. Expected to be
     *                       the simplename of the parent resource
     * @param idClazz        The type of the identifier for the parent resource
     * @param relatedIdClazz The type of the identifier for the related resource
     * @return PathItem documenting the DELETE/PUT many related items
     */
    private PathItem createRelatedItemPath(String tag, Class<?> idClazz,
                                           Class<?> relatedIdClazz) {

        var pi = new PathItem();

        ApiResponses defaultResponses = new ApiResponses().addApiResponse("200", emptyResponse);

        var idParameter = getParameter(idClazz, "path", "id");
        var relatedIdParameter = getArrayParameter(relatedIdClazz, "path", "related_id");
        var delete = new Operation();
        delete.addParametersItem(idParameter);
        delete.addParametersItem(relatedIdParameter);
        delete.setResponses(defaultResponses);
        delete.addTagsItem(tag);
        pi.delete(delete);

        var put = new Operation();
        put.addParametersItem(idParameter);
        put.addParametersItem(relatedIdParameter);
        put.setResponses(defaultResponses);
        put.addTagsItem(tag);
        pi.put(put);

        return pi;
    }

    /**
     * The {@link uk.gov.homeoffice.digital.sas.jparest.web.ApiResponse ApiResponse}
     * can return any type of resource in its items
     * property. This method returns a swagger ApiResponse that
     * contains a schema for the
     * {@link uk.gov.homeoffice.digital.sas.jparest.web.ApiResponse ApiResponse}
     * with its items set to the specified class
     *
     * @param clazz The type of items to describe in the schema
     * @return
     */
    private ApiResponse getResourceResponse(Class<?> clazz) {
        var response = new ApiResponse();

        var c = new Content();
        var mt = new MediaType();
        Schema<?> responseSchema = getTypedApiResponseSchema(clazz);
        mt.schema(responseSchema);
        c.addMediaType("*/*", mt);
        response.content(c);

        return response;
    }

    /**
     * Returns a schema for the
     * {@link uk.gov.homeoffice.digital.sas.jparest.web.ApiResponse ApiResponse}
     * class with the items property containing a schema for the given class
     *
     * @param clazz The type to generate a schema for
     * @return Schema for the ApiResponse
     */
    private Schema<?> getTypedApiResponseSchema(Class<?> clazz) {

        // Generate a schema for the ApiResponse
        Schema<?> schema = ModelConverters.getInstance()
                .read(new AnnotatedType(uk.gov.homeoffice.digital.sas.jparest.web.ApiResponse.class)
                        .resolveAsRef(false))
                .get("ApiResponse");

        // Get the schema for the given class
        var newComponents = new Components();
        Schema<?> clazzSchema = SpringDocAnnotationsUtils.extractSchema(newComponents, clazz, null, null);

        // Set the schema of the items property to the class schema
        var arraySchema = (ArraySchema) schema.getProperties().get("items");
        arraySchema.setItems(clazzSchema);

        return schema;

    }

    /**
     * Generates a parameter for the specified class
     * with the given name
     *
     * @param clazz The parameter type
     * @param setIn Where the parameter is set
     * @param name  The name of the parameter
     * @return A Parameter with a schema for the given class
     */
    private Parameter getParameter(Class<?> clazz, String setIn, String name) {
        var parameter = new Parameter();

        Schema<?> schema = SpringDocAnnotationsUtils.extractSchema(null, clazz, null, null);

        parameter.schema(schema);
        parameter.setIn(setIn);
        parameter.required(true);
        parameter.name(name);

        return parameter;
    }

    /**
     * Generates a parameter for an array of the specified class
     * with the given name
     *
     * @param clazz The parameter type
     * @param setIn Where the parameter is set
     * @param name  The name of the parameter
     * @return A Parameter with an array schema for items of the given class
     */
    private Parameter getArrayParameter(Class<?> clazz, String setIn, String name) {
        var parameter = new Parameter();
        Schema<?> schema = SpringDocAnnotationsUtils.extractSchema(null, clazz, null, null);
        var as = new ArraySchema();
        as.setItems(schema);

        parameter.schema(as);
        parameter.setIn(setIn);
        parameter.required(true);
        parameter.name(name);

        return parameter;
    }

    /**
     * Generates an empty/unspecified response.
     * This will be replaced as the ResourceApiController
     * is refined to define all expected response
     *
     * @return An empty ApiResponse
     */
    private static ApiResponse emptyResponse() {
        var deleteResponse = new io.swagger.v3.oas.models.responses.ApiResponse();
        var deleteContent = new Content();
        var deleteMediaType = new MediaType();
        deleteMediaType.schema(new StringSchema());
        deleteContent.addMediaType("*/*", deleteMediaType);
        deleteResponse.content(deleteContent);
        return deleteResponse;

    }

    /**
     * @return Parameter representing pageable class
     */
    private static Parameter pageableParameter() {
        var parameter = new Parameter();
        var newComponents = new Components();

        Schema<?> schema = SpringDocAnnotationsUtils.extractSchema(newComponents, Pageable.class, null, null);

        parameter.schema(schema);
        parameter.required(true);
        parameter.setIn("query");
        parameter.name("pageable");
        return parameter;

    }

    /**
     * @return Parameter representing SpelExpression
     */
    private static Parameter getFilterParameter(Class<?> clazz) {
        var parameter = new Parameter();
        var newComponents = new Components();

        Schema<?> schema = SpringDocAnnotationsUtils.extractSchema(newComponents, String.class, null, null);
        
        parameter.schema(schema);
        parameter.required(false);
        parameter.setIn("query");
        parameter.name("filter");

        Resource annotation = clazz.getAnnotation(Resource.class);
        for(ExampleObject exampleObj : annotation.filterExamples()) {
            var example = AnnotationsUtils.getExample(exampleObj).orElseThrow(() ->
                    new IllegalArgumentException(String.format(
                            "Example could not be found in ExampleObject with name: '%s' and ref: '%s'", exampleObj.name(), exampleObj.ref())));
            parameter.addExample(exampleObj.name(), example);
        }

        return parameter;

    }

    /**
     * Util function to get or create schema
     */
    private static Schema<?> ensureSchema(Components components, String schemaName,
                                          Class<?> clazz) {

        Schema<?> schema = components.getSchemas().get(schemaName);
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
