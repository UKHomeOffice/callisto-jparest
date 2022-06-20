package uk.gov.homeoffice.digital.sas.jparesttest.steps;

import io.cucumber.java.en.Then;
import net.thucydides.core.annotations.Steps;
import uk.gov.homeoffice.digital.sas.jparesttest.stepLib.ApiActions;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.homeoffice.digital.sas.jparesttest.stepLib.ApiActions.*;

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

    @Then("^A (\\d+) status code is returned$")
    public void iShouldGetBack(int responseCode) {
        apiActions.checkStatusCode(responseCode);
        assertThat("Status code does not match", apiActions.getResponseStatusCode(), is(responseCode));
    }

    @Then("^The \"([^\"]*)\" value from the \"([^\"]*)\" response is saved$")
    public void request(String value, String responseType) {
        switch (responseType) {
            case "Json Array":
                ApiActions.savedValue = apiActions.getResponseValueFromArrayOfKey(value).get(0).toString();
                break;
            case "Json Object":
                apiActions.saveBearerToken(apiActions.getResponseBody());
                break;
        }
    }
}
