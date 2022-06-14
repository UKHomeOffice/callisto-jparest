package stepLib;

import com.google.gson.JsonElement;
import io.restassured.RestAssured;
import net.serenitybdd.core.environment.EnvironmentSpecificConfiguration;
import net.thucydides.core.annotations.Step;
import net.thucydides.core.util.EnvironmentVariables;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Configuration;

import java.util.ArrayList;

import static net.serenitybdd.rest.SerenityRest.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


public class ApiActions {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiActions.class);

    private static EnvironmentVariables environmentVariables;


    private static String getEnvironmentProperty(String propertyName){
        return EnvironmentSpecificConfiguration.from(environmentVariables)
                .getProperty(propertyName);
    }

    // Return appropriate service name for Environment based on passed value
    private static String environmentVariableServiceName(String name){
        String serviceName = "Service Name not Found";
        if(name.contains("tracking"))
            serviceName= "accruals-tracking.service";
        else if(name.contains("population"))
            serviceName= "test-data-population.service";
        else if(name.contains("accrual"))
            serviceName= "accrual.service";
        else if(name.contains("timecard"))
            serviceName= "timecard.service";
        else if(name.contains("jparestapi"))
            serviceName= "jparestapi.service";
        else if(name.contains("people"))
            serviceName= "people.service";
        return serviceName;
    }

    @Step
    public int getResponseStatusCode() {
        return then().extract().statusCode();
    }

    @Step
    public String getResponseBody() {
        return then().extract().response().asString();
    }

    public static String profileId;

    public static String concertId;

    public static String recordId;

    public static String artistId;

    public static JsonElement generatedJson;

    public static String endpointRoot;

    public static String bearerToken;

    public static final String TENANT_ID = "tenantId";

    @Step
    public void restEndpointIsAvailable(String name) {
        String locator = name.toLowerCase().replace(' ', '.') + ".endpoint";
        LOGGER.info(locator);
        //This will find the apiRoute defined in the default.properties file
        String apiRoute = Configuration.get(locator);
        if(apiRoute==null)apiRoute="";
        //  URL endpoint = URL
        //This will find the host for the url using the serenity.conf file
        //Then appending the host file with the apiRoute that was assigned above
        //environmentVariableServiceName function just does a match on name and assigns a string
        //in order to find the right endpoint within the serenity.conf file
        endpointRoot = apiRoute;
        Assert.assertNotNull(endpointRoot);
        LOGGER.info(endpointRoot);
    }

    @Step

    public void getEndpoint() {
        given()
                .log().all()
                .when()
                .get(endpointRoot);
    }

    @Step
    public void getEndpointWithParam(String parameter) {
        given()
                .log().all()
                .when()
                .get(endpointRoot  + parameter);
    }


    @Step
    public void getEndpointWithBearerAndParam(String parameter) {
        given().headers("X-SPOOF-AUTHORIZATION",
                "Bearer " + bearerToken)
                .log().all()
                .when()
                .get(endpointRoot  + parameter);
    }

    @Step
    public void getEndpointWithParamAndBearer(String parameter) {
        given().headers("X-SPOOF-AUTHORIZATION",
                "Bearer " + bearerToken,
                "Content-Type", "application/json")
                .log().all()
                .when()
                .get(endpointRoot + parameter);
    }

    @Step
    public void getEndpointWithParamAndTenantId(String parameter, String tenantId) {
        given()
                .queryParams(TENANT_ID, tenantId)
                .contentType("application/json")
                .log().all()
                .when()
                .get(endpointRoot + parameter);
    }

    @Step
    public void getEndpointWithParamAndInvalidBearer(String parameter) {
        given().headers("X-SPOOF-AUTHORIZATION",
                "Bearer " + "INVALID_BEARER",
                "Content-Type", "application/json")
                .log().all()
                .when()
                .get(endpointRoot + parameter);
    }

    @Step
    public void postEndpoint(JsonElement json) {
        given().header("Content-Type", "application/json")
                .log().all()
                .when()
                .body(json.toString())
                .post(endpointRoot);
    }

    @Step
    public void postEndpointWithBearer(JsonElement json, String parameter) {
        given().headers("X-SPOOF-AUTHORIZATION",
                "Bearer " + bearerToken,
                "Content-Type", "application/json")
                .log().all()
                .when()
                .body(json.toString())
                .post(endpointRoot + parameter);
    }

    @Step
    public void postEndpointWithParamAndTenantId(JsonElement json, String parameter, String tenantId) {
        given()
                .queryParams(TENANT_ID, tenantId)
                .contentType("application/json")
                .log().all()
                .when()
                .body(json.toString())
                .post(endpointRoot + parameter);
    }

    @Step
    public void postEndpointWithInvalidBearer(JsonElement json) {
        given().headers("X-SPOOF-AUTHORIZATION",
                "Bearer " + "INVALID_BEARER",
                "Content-Type", "application/json")
                .log().all()
                .when()
                .body(json.toString())
                .post(endpointRoot);
    }

    @Step
    public void emptyPostEndpointWithParam(String parameter) {
        given()
                .log().all()
                .when()
                .post(endpointRoot  + parameter);
    }

    @Step
    public void putEndpointWithBearer(JsonElement json, String parameter) {
        given().headers("X-SPOOF-AUTHORIZATION",
                "Bearer " + bearerToken,
                "Content-Type", "application/json")
                .log().all()
                .when()
                .body(json.toString())
                .put(endpointRoot + parameter);
    }

    @Step
    public void putEndpointWithParamAndTenantId(JsonElement json, String parameter, String tenantId) {
        given()
                .queryParams(TENANT_ID, tenantId)
                .contentType("application/json")
                .log().all()
                .when()
                .body(json.toString())
                .put(endpointRoot + parameter);
    }

    @Step
    public void deleteEndpoint(String parameter) {
        given().header("Content-Type", "application/json")
                .log().all()
                .when()
                .delete(endpointRoot + parameter);
    }

    @Step
    public void deleteEndpointWithParamAndTenantId(String parameter, String tenantId) {
        given()
                .queryParams(TENANT_ID, tenantId)
                .contentType("application/json")
                .log().all()
                .when()
                .delete(endpointRoot + parameter);
    }

    @Step
    public void setEndpoint() {
            RestAssured.baseURI = endpointRoot;
    }

    @Step
    public void getResource(String resourceName) {
        String resourcePath = Configuration.get(resourceName);
        when().get(resourcePath);
    }

    @Step
    public ArrayList getResponseValueFromArrayOfKey(String key) {
        return then().extract().path(key);
    }

    @Step
    public String getResponseValueFromKey(String key) {
        return then().extract().path(key);
    }


    @Step
    public void checkStatusCode(int code) {
        assertThat("Status code does not match", getResponseStatusCode(), is(code));
    }

    @Step
    public void saveBearerToken(String bearer) {
        bearerToken = bearer;
    }


}
