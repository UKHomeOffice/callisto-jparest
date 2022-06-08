package uk.gov.homeoffice.digital.sas.jparest.swagger;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.util.AnnotationsUtils;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.SpringDocAnnotationsUtils;
import org.springdoc.core.converters.models.Pageable;
import org.springframework.stereotype.Component;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;

import java.util.UUID;

import static uk.gov.homeoffice.digital.sas.jparest.utils.ConstantHelper.ID_PARAM_NAME;
import static uk.gov.homeoffice.digital.sas.jparest.utils.ConstantHelper.RELATED_PARAM_NAME;

@Component
public class PathItemCreator {

    private static final ApiResponse EMPTY_RESPONSE = emptyResponse();
    public static final String PAGEABLE = "pageable";
    public static final String QUERY_PARAMETER_NAME = "query";
    private static final Parameter PAGEABLE_PARAMETER = getParameter(Pageable.class, QUERY_PARAMETER_NAME, PAGEABLE);

    private static final Logger LOGGER = LoggerFactory.getLogger(PathItemCreator.class);
    private static final String TENANT_ID = "tenantId";
    public static final Parameter TENANT_ID_PARAMETER = getParameter(UUID.class, QUERY_PARAMETER_NAME, TENANT_ID);
    public static final String PATH = "path";


    /**
     * Creates documentation for the endpoints of the resource
     * covers get many and post
     *
     * @param tag   The tag to group the endpoints together. Expected to be the
     *              simplename of the resource
     * @param clazz The class representing the resource exposed by the endpoint
     * @return PathItem documenting the GET many and POST endpoints
     */
    public PathItem createRootPath(String tag, Class<?> clazz) {

        var pi = new PathItem();

        ApiResponse response = getResourceResponse(clazz);
        ApiResponses responses = new ApiResponses().addApiResponse("200", response);


        var get = new Operation();
        get.addParametersItem(TENANT_ID_PARAMETER);
        get.addParametersItem(PAGEABLE_PARAMETER);
        get.addParametersItem(getFilterParameter(clazz));
        get.setResponses(responses);
        get.addTagsItem(tag);
        pi.get(get);

        var post = new Operation();
        post.setResponses(responses);
        post.addParametersItem(TENANT_ID_PARAMETER);
        post.addTagsItem(tag);
        var requestBody = getRequestBody(clazz);
        post.setRequestBody(requestBody);

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
    public PathItem createItemPath(String tag, Class<?> clazz, Class<?> idClazz) {

        var pi = new PathItem();

        ApiResponse response = getResourceResponse(clazz);
        ApiResponses responses = new ApiResponses().addApiResponse("200", response);
        var get = new Operation();
        get.setResponses(responses);
        get.addTagsItem(tag);
        pi.get(get);

        var idParameter = getParameter(idClazz, PATH, ID_PARAM_NAME);
        get.addParametersItem(idParameter);
        get.addParametersItem(TENANT_ID_PARAMETER);
        var put = new Operation();
        put.addParametersItem(idParameter);
        var requestBody = getRequestBody(clazz);
        put.setRequestBody(requestBody);
        put.setResponses(responses);
        put.addParametersItem(TENANT_ID_PARAMETER);
        put.addTagsItem(tag);
        pi.put(put);
        var delete = new Operation();

        ApiResponses deleteResponses = new ApiResponses().addApiResponse("200", EMPTY_RESPONSE);
        delete.addParametersItem(idParameter);
        delete.addParametersItem(TENANT_ID_PARAMETER);
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
    public PathItem createRelatedRootPath(String tag, Class<?> clazz, Class<?> idClazz) {

        var pi = new PathItem();
        ApiResponse response = getResourceResponse(clazz);
        ApiResponses responses = new ApiResponses().addApiResponse("200", response);
        var idParameter = getParameter(idClazz, PATH, ID_PARAM_NAME);

        var get = new Operation();
        get.addParametersItem(idParameter);
        get.addParametersItem(TENANT_ID_PARAMETER);
        get.addParametersItem(PAGEABLE_PARAMETER);
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
    public PathItem createRelatedItemPath(String tag, Class<?> idClazz,
                                           Class<?> relatedIdClazz) {

        var pi = new PathItem();

        ApiResponses defaultResponses = new ApiResponses().addApiResponse("200", EMPTY_RESPONSE);

        var idParameter = getParameter(idClazz, PATH, ID_PARAM_NAME);
        var relatedIdParameter = getArrayParameter(relatedIdClazz, PATH, RELATED_PARAM_NAME);
        var delete = new Operation();
        delete.addParametersItem(idParameter);
        delete.addParametersItem(relatedIdParameter);
        delete.addParametersItem(TENANT_ID_PARAMETER);
        delete.setResponses(defaultResponses);
        delete.addTagsItem(tag);
        pi.delete(delete);

        var put = new Operation();
        put.addParametersItem(idParameter);
        put.addParametersItem(TENANT_ID_PARAMETER);
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
    private static ApiResponse getResourceResponse(Class<?> clazz) {
        var response = new ApiResponse();

        var c = new Content();
        var mt = new MediaType();
        Schema<?> responseSchema = getTypedApiResponseSchema(clazz);
        mt.schema(responseSchema);
        c.addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE, mt);
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
    private static Schema<?> getTypedApiResponseSchema(Class<?> clazz) {

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
    private static Parameter getParameter(Class<?> clazz, String setIn, String name) {
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
    private static Parameter getArrayParameter(Class<?> clazz, String setIn, String name) {
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

    /**This method returns a swagger RequestBody that
     * contains a schema for the specified class
     *
     * @param clazz The type of item to describe in the schema
     * @return
     */
    private static RequestBody getRequestBody(Class<?> clazz) {

        var c = new Content();
        var mt = new MediaType();
        Schema<?> schema = SpringDocAnnotationsUtils.extractSchema(null, clazz, null, null);
        mt.schema(schema);
        c.addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE, mt);

        var requestBody = new RequestBody();
        requestBody.setContent(c);
        return requestBody;
    }

    /**
     * Generates an empty/unspecified response.
     * This will be replaced as the ResourceApiController
     * is refined to define all expected response
     *
     * @return An empty ApiResponse
     */
    private static ApiResponse emptyResponse() {
        var deleteResponse = new ApiResponse();
        var deleteContent = new Content();
        var deleteMediaType = new MediaType();
        deleteMediaType.schema(new StringSchema());
        deleteContent.addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE, deleteMediaType);
        deleteResponse.content(deleteContent);
        return deleteResponse;

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
        parameter.setIn(QUERY_PARAMETER_NAME);
        parameter.name("filter");

        Resource annotation = clazz.getAnnotation(Resource.class);
        for(ExampleObject exampleObj : annotation.filterExamples()) {

            var exampleOpt = AnnotationsUtils.getExample(exampleObj);
            if (exampleOpt.isPresent()) {
                parameter.addExample(exampleObj.name(), exampleOpt.get());
            } else {
                LOGGER.error("Example could not be found in ExampleObject from resource: {} ", clazz.getSimpleName());
            }
        }
        return parameter;

    }


}