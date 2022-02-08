package uk.gov.homeoffice.digital.sas.jparest.swagger;

import org.springdoc.core.customizers.OpenApiCustomiser;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.Operation;

/**
 * Extends the OpenApi model to include the endpoints added by the resource annotation
 */
public class ResourceOpenApiCustomiser implements OpenApiCustomiser {

    public void customise(OpenAPI openApi) {
        PathItem pi = new PathItem();
        ApiResponse response = new ApiResponse();
        ApiResponses responses = new ApiResponses().addApiResponse("200", response);
        Operation put = new Operation();
        put.setResponses(responses);
        pi.put(put);
        put.addTagsItem("Example");
        openApi.path("/resources/jpaentity", pi);
    }

}
    