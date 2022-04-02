package uk.gov.homeoffice.digital.sas.jparest.swagger.testutils;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

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

    public static PathItem createGetPathItem() {
        var pathItem = new PathItem();
        pathItem.get(new Operation());
        return pathItem;
    }

    public static PathItem createPostPathItem() {
        var pathItem = new PathItem();
        pathItem.post(new Operation());
        return pathItem;
    }

    public static PathItem createPutPathItem() {
        var pathItem = new PathItem();
        pathItem.put(new Operation());
        return pathItem;
    }

    public static PathItem createDeletePathItem() {
        var pathItem = new PathItem();
        pathItem.delete(new Operation());
        return pathItem;
    }





}
