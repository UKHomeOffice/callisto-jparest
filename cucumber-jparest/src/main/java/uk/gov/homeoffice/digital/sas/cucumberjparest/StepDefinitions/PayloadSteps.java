package uk.gov.homeoffice.digital.sas.cucumberjparest.StepDefinitions;

import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.java.en.Given;
import lombok.NonNull;
import uk.gov.homeoffice.digital.sas.cucumberjparest.Interpolation;
import uk.gov.homeoffice.digital.sas.cucumberjparest.PayloadManager;
import uk.gov.homeoffice.digital.sas.cucumberjparest.PayloadManager.PayloadKey;

public class PayloadSteps {

    private final PayloadManager payloadManager;
    private final Interpolation interpolation;

    @Autowired
    public PayloadSteps(@NonNull PayloadManager payloadManager, Interpolation interpolation) {
        this.payloadManager = payloadManager;
        this.interpolation = interpolation;

    }

    /**
     * 
     * Registers a payload that can be used in the scenario
     * 
     * @param payloadKey The key for the payload (resource type and name)
     * @param docString  The content of the payload
     */
    @Given("{payload} are")
    public void inline_resources_are(PayloadKey payloadKey, String docString) {
        String interpolatedPayload = interpolation.Evaluate(docString);
        payloadManager.createPayload(payloadKey, interpolatedPayload);
    }

}
