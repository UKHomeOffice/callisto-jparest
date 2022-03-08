package uk.gov.homeoffice.digital.sas.jparest.swagger.testutils;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.SpringDocAnnotationsUtils;

public class ApiResponseTestUtil {

    private ApiResponseTestUtil() {
        // no instantiation
    }


    public static ApiResponse createDefaultApiResponse(Class<?> resourceClass) {
        var apiResponse = new ApiResponse();
        Content content = new Content();
        MediaType mediaType = new MediaType();
        Schema<?> responseSchema = getApiResponseSchema(resourceClass);
        mediaType.schema(responseSchema);
        content.addMediaType("*/*", mediaType);
        apiResponse.content(content);

        return apiResponse;
    }

    public static ApiResponse createEmptyApiResponse() {
        var apiResponse = new ApiResponse();
        var content = new Content();
        MediaType mediaType = new MediaType();
        mediaType.schema(new StringSchema());
        content.addMediaType("*/*", mediaType);
        apiResponse.content(content);

        return apiResponse;
    }


    private static Schema<?> getApiResponseSchema(Class<?> clazz) {

        Schema<?> schema = ModelConverters.getInstance()
                .read(new AnnotatedType(uk.gov.homeoffice.digital.sas.jparest.web.ApiResponse.class)
                        .resolveAsRef(false))
                .get("ApiResponse");

        Schema<?> clazzSchema = SpringDocAnnotationsUtils.extractSchema(new Components(), clazz, null, null);
        var arraySchema = (ArraySchema) schema.getProperties().get("items");
        arraySchema.setItems(clazzSchema);

        return schema;
    }

}
