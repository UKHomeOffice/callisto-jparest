package uk.gov.homeoffice.digital.sas.jparesttest.steps;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.cucumber.java.en.Then;
import net.thucydides.core.annotations.Steps;
import uk.gov.homeoffice.digital.sas.jparesttest.stepLib.ApiActions;

import static uk.gov.homeoffice.digital.sas.jparesttest.stepLib.ApiActions.*;

public class JsonGenerators {
    
    @Steps
    private static ApiActions apiActions;

    @Steps
    private static RequestJson requestJson;

    @Then("^A json file for \"([^\"]*)\" endpoint is created: \"(.*)\"$")
    public JsonElement defineRequestJson(String identifier, String value) {
        JsonParser jsonParser = new JsonParser();
        generatedJson = new JsonObject();
        generatedJson = jsonParser.parse(requestJson.createJsonObject(value, dynamicData));
        return generatedJson;
    }
}
