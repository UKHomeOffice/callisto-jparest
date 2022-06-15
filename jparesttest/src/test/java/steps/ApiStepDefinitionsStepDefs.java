package steps;

import io.cucumber.java.en.Then;
import java.util.LinkedList;
import static org.mockito.Mockito.*;

public class ApiStepDefinitionsStepDefs {

    LinkedList mockedList = mock(LinkedList.class);

    @Then("^I call the \"([^\"]*)\" \"([^\"]*)\" endpoint with \"([^\"]*)\" and parameter \"([^\"]*)\"$")
    public void request(String requestType, String endpoint, String bearerToken, String param) throws InterruptedException {

        when(mockedList.get(0)).thenReturn(200);
        System.out.println("****************");
        System.out.println(mockedList.get(0));
        System.out.println("****************");

    }

}
