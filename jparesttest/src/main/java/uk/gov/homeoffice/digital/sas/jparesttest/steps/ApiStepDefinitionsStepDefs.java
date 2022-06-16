package uk.gov.homeoffice.digital.sas.jparesttest.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.thucydides.core.annotations.Steps;
import uk.gov.homeoffice.digital.sas.jparesttest.stepLib.ApiActions;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ApiStepDefinitionsStepDefs {

    @Steps
    private static ApiActions apiActions;

    @Then("A call to the GET endpoint has been made")
    public void request() throws Exception {
        System.out.println("****************");
        System.out.println("hello");
        System.out.println("****************");
    }

    @Then("^as a tester I call the \"([^\"]*)\" \"([^\"]*)\" endpoint with \"([^\"]*)\" and the parameter \"([^\"]*)\"$")
    public void request(String requestType, String endpoint, String bearerToken, String param) {
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
