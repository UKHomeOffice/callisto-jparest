package uk.gov.homeoffice.digital.sas.cucumberjparest;

import io.restassured.path.json.JsonPath;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * Rest assured JsonPath objects represented a captured
 * reference to one or more resources from a response.
 * 
 * The object contains the resource type as well as the
 * objects as sometimes steps will require the resource
 * type. The JsonPath may represent a single resource
 * or a list of resources
 * 
 */
@AllArgsConstructor
public class Resource {

    @Getter
    @Setter
    private String resourceType;

    @Getter
    @Setter
    private JsonPath jsonPath;
}