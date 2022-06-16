package uk.gov.homeoffice.digital.sas.jparesttest.steps;

import io.cucumber.java.en.Then;
import net.thucydides.core.annotations.Steps;
import uk.gov.homeoffice.digital.sas.jparesttest.stepLib.ApiActions;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ApiStepDefinitionsStepDefs {

    @Steps
    private static ApiActions apiActions;
    public static String profileId;

    @Then("^as a tester I call the \"([^\"]*)\" \"([^\"]*)\" endpoint with \"([^\"]*)\" value \"([^\"]*)\" and the parameter \"([^\"]*)\"$")
    public void request(String requestType, String endpoint, String key, String value, String param) {
        apiActions.restEndpointIsAvailable(endpoint);
        apiActions.setEndpoint();
        if(param.contains("dynamic")) param = ApiActions.dynamicData;
        switch (requestType) {
            case "RETRIEVE" -> apiActions.getEndpointWithQueryParam(param, key, value);
            case "REMOVE" -> apiActions.deleteEndpointWithQueryParam(param, key, value);
        }
    }

    @Then("^I should get (\\d+) back$")
    public void iShouldGetBack(int responseCode) {
        apiActions.checkStatusCode(responseCode);
        assertThat("Status code does not match", apiActions.getResponseStatusCode(), is(responseCode));
    }

    @Then("^The \"([^\"]*)\" value from the response \"([^\"]*)\" is saved$")
    public void request(String value, String responseType) {
        switch (responseType) {
            case "Array":
                ApiActions.dynamicData = apiActions.getResponseValueFromArrayOfKey(value).get(0).toString();
                break;
            case "Object":
                apiActions.saveBearerToken(apiActions.getResponseBody());
                break;
        }
    }
}
