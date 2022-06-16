package steps;

import io.cucumber.java.en.Then;

public class ApiStepDefinitionsStepDefs {

    @Then("A call to the GET endpoint has been made")
    public void request() {
        System.out.println("****************");
        System.out.println("hello");
        System.out.println("****************");
    }
}
