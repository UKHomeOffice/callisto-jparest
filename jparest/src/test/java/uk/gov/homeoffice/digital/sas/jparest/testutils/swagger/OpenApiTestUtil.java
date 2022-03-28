package uk.gov.homeoffice.digital.sas.jparest.testutils.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;

import java.util.HashMap;

public class OpenApiTestUtil {

    private OpenApiTestUtil() {
        // no instantiation
    }


    public static OpenAPI createDefaultOpenAPI() {
        var openApi = new OpenAPI();
        var components = new Components();
        components.schemas(new HashMap<>());
        openApi.setComponents(components);
        return openApi;
    }





}
