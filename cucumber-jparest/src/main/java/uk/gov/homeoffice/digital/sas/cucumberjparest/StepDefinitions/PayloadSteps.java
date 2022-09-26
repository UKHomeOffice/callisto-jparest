package uk.gov.homeoffice.digital.sas.cucumberjparest.StepDefinitions;

import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.java.en.Given;
import lombok.NonNull;
import uk.gov.homeoffice.digital.sas.cucumberjparest.PayloadManager;

public class PayloadSteps {

    private final PayloadManager payloadManager;

    @Autowired
    public PayloadSteps(@NonNull PayloadManager payloadManager) {
        this.payloadManager = payloadManager;

    }

    /**
     * 
     * Registers a payload that can be used in the scenario
     * 
     * @param payloadName  The name to give the payload
     * @param resourceType The resource being represented
     * @param docString    The content of the payload
     */
    @Given("{word} {word} are")
    public void inline_resources_are(String payloadName, String resourceType, String docString) {
        payloadManager.createPayload(payloadName, resourceType, docString);
    }
}
