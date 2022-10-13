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
import org.springframework.stereotype.Component;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;
import uk.gov.homeoffice.digital.sas.jparest.controller.enums.RequestParameter;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class PathItemCreator {

    private static final ApiResponse EMPTY_RESPONSE = emptyResponse();
    private static final Parameter ID_PARAMETER = getParameter(RequestParameter.ID);
    private static final Parameter PAGEABLE_PARAMETER = getParameter(RequestParameter.PAGEABLE);
    private static final Parameter TENANT_ID_PARAMETER = getParameter(RequestParameter.TENANT_ID);

    private static final Map<String, RequestParameter> PARAM_NAME_TO_ENUM_MAP
        = RequestParameter.getParamNameToEnumMap();

    private static final Logger LOGGER = LoggerFactory.getLogger(PathItemCreator.class);


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
        addParametersToOperation(get, TENANT_ID_PARAMETER, PAGEABLE_PARAMETER, getFilterParameter(clazz));
        get.setResponses(responses);
        get.addTagsItem(tag);
        pi.get(get);

        var post = new Operation();
        post.setResponses(responses);
        addParametersToOperation(post, TENANT_ID_PARAMETER);
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
     * @return PathItem documenting the GET one and PUT endpoints
     */
    public PathItem createItemPath(String tag, Class<?> clazz) {

        var pi = new PathItem();

        ApiResponse response = getResourceResponse(clazz);
        ApiResponses responses = new ApiResponses().addApiResponse("200", response);

        var get = new Operation();
        get.setResponses(responses);
        get.addTagsItem(tag);
        pi.get(get);
        addParametersToOperation(get, TENANT_ID_PARAMETER, ID_PARAMETER);

        var put = new Operation();
        addParametersToOperation(put, TENANT_ID_PARAMETER, ID_PARAMETER);
        var requestBody = getRequestBody(clazz);
        put.setRequestBody(requestBody);
        put.setResponses(responses);
        put.addTagsItem(tag);
        pi.put(put);

        var delete = new Operation();
        ApiResponses deleteResponses = new ApiResponses().addApiResponse("200", EMPTY_RESPONSE);
        addParametersToOperation(delete, TENANT_ID_PARAMETER, ID_PARAMETER);
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
     * @return PathItem documenting the GET many related items endpoint
     */
    public PathItem createRelatedRootPath(String tag, Class<?> clazz) {

        var pi = new PathItem();
        ApiResponse response = getResourceResponse(clazz);
        ApiResponses responses = new ApiResponses().addApiResponse("200", response);

        var get = new Operation();
        addParametersToOperation(get, TENANT_ID_PARAMETER, ID_PARAMETER, PAGEABLE_PARAMETER, getFilterParameter(clazz));
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
     * @return PathItem documenting the DELETE/PUT many related items
     */
    public PathItem createRelatedItemPath(String tag) {

        var pi = new PathItem();

        ApiResponses defaultResponses = new ApiResponses().addApiResponse("200", EMPTY_RESPONSE);

        var relatedIdParameter = getArrayParameter(UUID.class, RequestParameter.RELATED_IDS);
        var delete = new Operation();
        addParametersToOperation(delete, TENANT_ID_PARAMETER, ID_PARAMETER, relatedIdParameter);
        delete.setResponses(defaultResponses);
        delete.addTagsItem(tag);
        pi.delete(delete);

        var put = new Operation();
        addParametersToOperation(put, TENANT_ID_PARAMETER, ID_PARAMETER, relatedIdParameter);
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
     * based on the given RequestParameter type
     *
     * @param requestParameter the parameter information used to create the Swagger Parameter
     * @return A Parameter with a schema for the given class
     */
    private static Parameter getParameter(RequestParameter requestParameter) {

        var parameter = new Parameter();
        Schema<?> schema = SpringDocAnnotationsUtils.extractSchema(
            null, requestParameter.getParamDataType(),  null, null);

        parameter.schema(schema);
        parameter.setIn(requestParameter.getParamType());
        parameter.required(requestParameter.isRequired());
        parameter.name(requestParameter.getParamName());

        return parameter;
    }

    /**
     * Generates a parameter for an array of the specified class
     * based on the given RequestParameter type
     *
     * @param clazz The parameter type
     * @param requestParameter the parameter information used to create the Swagger Parameter
     * @return A Parameter with an array schema for items of the given class
     */
    private static Parameter getArrayParameter(Class<?> clazz, RequestParameter requestParameter) {
        var parameter = new Parameter();
        Schema<?> schema = SpringDocAnnotationsUtils.extractSchema(null, clazz, null, null);
        var as = new ArraySchema();
        as.setItems(schema);

        parameter.schema(as);
        parameter.setIn(requestParameter.getParamType());
        parameter.required(requestParameter.isRequired());
        parameter.name(requestParameter.getParamName());

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
        parameter.required(RequestParameter.FILTER.isRequired());
        parameter.setIn(RequestParameter.FILTER.getParamType());
        parameter.name(RequestParameter.FILTER.getParamName());

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


    private void addParametersToOperation(Operation operation, Parameter... parameters) {
        Arrays.stream(parameters)
                .sorted(Comparator.comparing(param -> Optional.of(PARAM_NAME_TO_ENUM_MAP.get(
                    param.getName())).orElseThrow(() ->
                                new IllegalArgumentException(
                                    String.format("No %s enum constant found for parameter name: %s ",
                                        RequestParameter.class.getCanonicalName(), param.getName()))).getOrder()))
                .forEach(operation::addParametersItem);
    }


}