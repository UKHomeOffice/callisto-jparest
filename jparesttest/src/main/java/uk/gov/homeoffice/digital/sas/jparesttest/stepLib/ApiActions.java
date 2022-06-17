package uk.gov.homeoffice.digital.sas.jparesttest.stepLib;

import com.google.gson.JsonElement;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import net.serenitybdd.core.environment.EnvironmentSpecificConfiguration;
import net.serenitybdd.rest.SerenityRest;
import net.thucydides.core.annotations.Step;
import net.thucydides.core.util.EnvironmentVariables;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.homeoffice.digital.sas.jparesttest.utils.Configuration;

import java.util.ArrayList;

import static io.restassured.RestAssured.given;
import static net.serenitybdd.rest.SerenityRest.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ApiActions {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiActions.class);

    private static EnvironmentVariables environmentVariables;

    public static Response response;

    public static String savedValue;

    public static JsonElement generatedJson;

    private static String getEnvironmentProperty(String propertyName){
        return EnvironmentSpecificConfiguration.from(environmentVariables)
                .getProperty(propertyName);
    }

    // Return appropriate service name for Environment based on passed value
    private static String environmentVariableServiceName(String name){
        String serviceName = "Service Name not Found";
        if(name.contains("jparestapi"))
            serviceName= "jparestapi.service";
        return serviceName;
    }

    @Step
    public int getResponseStatusCode() {
        return response.getStatusCode();
    }

    @Step
    public String getResponseBody() {
        return response.getBody().asString();
    }

    public static String endpointRoot;

    public static String bearerToken;

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
    public void retrieveEndpointWithQueryParam(String key, String value) {
       response = given()
                .queryParams(key, value)
                .contentType("application/json")
                .log().all()
                .when()
                .get(endpointRoot);
    }

    public void removeEndpointWithQueryParam(String key, String value) {
        response = given()
                .queryParams(key, value)
                .contentType("application/json")
                .log().all()
                .when()
                .delete(endpointRoot);
    }

    public void saveEndpointWithQueryParam(JsonElement json, String key, String value) {
        response = given()
                .queryParams(key, value)
                .contentType("application/json")
                .log().all()
                .when()
                .body(json.toString())
                .post(endpointRoot);
    }

    @Step
    public void updateEndpointWithQueryParam(JsonElement json, String parameter, String key, String value) {
        response = given()
                .queryParams(key, value)
                .contentType("application/json")
                .log().all()
                .when()
                .body(json.toString())
                .put(endpointRoot + parameter);
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
        return response.path(key);
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

