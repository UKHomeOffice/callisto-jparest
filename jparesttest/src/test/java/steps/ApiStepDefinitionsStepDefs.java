package steps;

import io.cucumber.java.en.Then;
import java.util.LinkedList;
import static org.mockito.Mockito.*;

public class ApiStepDefinitionsStepDefs {

    LinkedList mockedList = mock(LinkedList.class);

    @Then("Test GET call")
    public void request() {
        System.out.println("****************");
        System.out.println("hello");
        System.out.println("****************");

    }

}
