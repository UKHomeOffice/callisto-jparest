package uk.gov.homeoffice.digital.sas.cucumberjparest.api;

import io.restassured.path.json.JsonPath;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * Represents a resource extracted from the response of an API request.
 * 
 * <p>The resource is represented by the type of the resource and a {@link JsonPath}
 * with its {@link JsonPath#setRootPath(String)} set to the resource(s).
 * The JsonPath may point to a single resource or a list of resources</p>
 *
 * <p>The object contains the resource type as well as the object as steps sometimes
 * need access to the resource type.</p>
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