package steps;

import io.cucumber.java.en.Then;
import net.thucydides.core.annotations.Steps;
import stepLib.ApiActions;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ApiStepDefinitions {

    @Steps
    private static ApiActions apiActions;

    @Then("^I call the \"([^\"]*)\" \"([^\"]*)\" endpoint with \"([^\"]*)\" and parameter \"([^\"]*)\"$")
    public void request(String requestType, String endpoint, String bearerToken, String param) throws InterruptedException {
        apiActions.restEndpointIsAvailable(endpoint);
        apiActions.setEndpoint();

        apiActions.getEndpointWithParamAndTenantId(param, bearerToken);
    }

    @Then("^I should get (\\d+) back$")
    public void iShouldGetBack(int responseCode) {
        apiActions.checkStatusCode(responseCode);
        assertThat("Status code does not match", apiActions.getResponseStatusCode(), is(responseCode));
    }

}
