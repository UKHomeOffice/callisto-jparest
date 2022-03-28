package uk.gov.homeoffice.digital.sas.jparest.testutils.swagger;

import io.swagger.v3.core.util.AnnotationsUtils;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.SpringDocAnnotationsUtils;
import org.springdoc.core.converters.models.Pageable;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;

import java.lang.reflect.Type;

import static uk.gov.homeoffice.digital.sas.jparest.utils.ConstantHelper.ID_PARAM_NAME;
import static uk.gov.homeoffice.digital.sas.jparest.utils.ConstantHelper.RELATED_PARAM_NAME;

public class HttpOperationTestUtil {


    private HttpOperationTestUtil() {
        // no instantiation
    }


    public static Operation createSuccessOperation(Class<?> resourceClass, ApiResponse apiResponse) {

        var operation = new Operation();
        operation.setResponses(new ApiResponses().addApiResponse("200", apiResponse));
        operation.addTagsItem(resourceClass.getSimpleName());
        return operation;
    }


    public static void addPageableParameterToOperation(Operation operation) {
        var parameter = new Parameter();
        parameter.schema(SpringDocAnnotationsUtils.extractSchema(new Components(), Pageable.class, null, null));
        parameter.setIn("query");
        parameter.required(true);
        parameter.name("pageable");
        operation.addParametersItem(parameter);
    }

    public static void addFilterParameterToOperation(Operation operation, Class<?> resourceClass) {
        var parameter = new Parameter();
        parameter.schema(SpringDocAnnotationsUtils.extractSchema(new Components(), String.class, null, null));
        parameter.setIn("query");
        parameter.required(false);
        parameter.name("filter");

        var annotation = resourceClass.getAnnotation(Resource.class);
        for(var example : annotation.filterExamples()) {
            parameter.addExample(example.name(), AnnotationsUtils.getExample(example).get());
        }
        operation.addParametersItem(parameter);
    }

    public static void addIdParameterToOperation(Operation operation, Type returnType) {
        var parameter = new Parameter();
        parameter.schema(SpringDocAnnotationsUtils.extractSchema(null, returnType, null, null));
        parameter.setIn("path");
        parameter.name(ID_PARAM_NAME);
        parameter.required(true);
        operation.addParametersItem(parameter);
    }

    public static void addArrayRelatedIdParameterToOperation(Operation operation, Type returnType) {
        var parameter = new Parameter();
        var schema = SpringDocAnnotationsUtils.extractSchema(null, returnType, null, null);
        var arraySchema = new ArraySchema();
        arraySchema.setItems(schema);
        parameter.schema(arraySchema);
        parameter.setIn("path");
        parameter.name(RELATED_PARAM_NAME);
        parameter.required(true);
        operation.addParametersItem(parameter);
    }

    public static void addRequestBody(Operation operation, Class<?> resourceClass) {
        var schema = SpringDocAnnotationsUtils.extractSchema(null, resourceClass, null, null);
        var requestBody = new RequestBody();
        requestBody.setContent(new Content().addMediaType("application/json", new MediaType().schema(schema)));
        operation.setRequestBody(requestBody);
    }





}
