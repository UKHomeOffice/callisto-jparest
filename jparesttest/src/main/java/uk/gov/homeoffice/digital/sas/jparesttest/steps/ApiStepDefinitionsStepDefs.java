package uk.gov.homeoffice.digital.sas.jparesttest.steps;

import io.cucumber.java.en.Then;
import net.thucydides.core.annotations.Steps;
import org.junit.Assert;
import uk.gov.homeoffice.digital.sas.jparesttest.stepLib.ApiActions;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.homeoffice.digital.sas.jparesttest.stepLib.ApiActions.*;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class ApiStepDefinitionsStepDefs {

    @Steps
    private static ApiActions apiActions;
    public static String profileId;

    @Then("^As a tester I call the \"([^\"]*)\" \"([^\"]*)\" endpoint(?: with query parameter key: \"([^\"]*)\" and value: \"([^\"]*)\")?(?: with URL parameter: \"([^\"]*)\")?$")
    public void request(String requestType, String endpoint, String key, String value, String param) {
        apiActions.restEndpointIsAvailable(endpoint);
        apiActions.setEndpoint();
        if(param == null) param = "";
        else if(param.equals("savedValue")) param = "/" + ApiActions.savedValue;
        switch (requestType) {
            case "retrieve" -> apiActions.retrieveEndpoint(param, key, value);
            case "save" -> apiActions.saveEndpoint(generatedJson, param, key, value);
            case "update" -> apiActions.updateEndpoint(generatedJson, param, key, value);
            case "remove" -> apiActions.removeEndpoint(param, key, value);
            default -> fail("Request type: " + requestType + " does not exist, please add to switch statement");
        }
    }

    @Then("^A successful (\\d+) status code is returned$")
    public void iShouldGetBack(int responseCode) {
        apiActions.checkStatusCode(responseCode);
        assertThat("Status code does not match", apiActions.getResponseStatusCode(), is(responseCode));
    }

    @Then("^The \"([^\"]*)\" value from the \"([^\"]*)\" response is saved(?: as: \"([^\"]*)\")?$")
    public void request(String value, String responseType, String idValue) {
        switch (responseType) {
            case "Json Array":
                    switch (idValue) {
                        case "idValueOne" -> idValueOne = apiActions.getResponseValueFromArrayOfKey(value).get(0).toString();
                        case "idValueTwo" -> idValueTwo = apiActions.getResponseValueFromArrayOfKey(value).get(0).toString();
                        case "idValueThree" -> idValueThree = apiActions.getResponseValueFromArrayOfKey(value).get(0).toString();
                        case "idValueFour" -> idValueFour = apiActions.getResponseValueFromArrayOfKey(value).get(0).toString();
                        default -> ApiActions.savedValue = apiActions.getResponseValueFromArrayOfKey(value).get(0).toString();
                    }
                    break;
            case "Json Object": apiActions.saveBearerToken(apiActions.getResponseBody());
                    break;
        }
    }

    @Then("^As a tester I check that the expected response is correct$")
    public void checkResponse() {
        JsonParser jsonParser = new JsonParser();
        JsonElement actualResponse = jsonParser.parse(apiActions.getResponseBody());
        JsonElement expectedResponse = generatedJson;
        Assert.assertEquals("Expected response: " + expectedResponse + " did not equal Actual response: " + actualResponse, expectedResponse, actualResponse);
    }


    @Then("^\"([^\"]*)\" has been assigned$")
    public void assignValue(String idValue) {
        switch (idValue) {
            case "idValueOne" -> savedValue = idValueOne;
            case "idValueTwo" -> savedValue = idValueTwo;
            case "idValueThree" -> savedValue = idValueThree;
            case "idValueFour" -> savedValue = idValueFour;
            default -> fail("idValue specified: " + idValue + " does not exist within the switch statement");
        }
    }
}
