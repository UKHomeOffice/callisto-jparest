package uk.gov.homeoffice.digital.sas.cucumberjparesttestapi.stepdefinitions;

import static org.assertj.core.api.Assertions.*;

import io.cucumber.java.en.Then;
import lombok.NonNull;
import uk.gov.homeoffice.digital.sas.cucumberjparest.api.PayloadManager;
import uk.gov.homeoffice.digital.sas.cucumberjparest.api.PayloadManager.PayloadKey;

public class PayloadManagerStep {

  private final PayloadManager payloadManager;

  public PayloadManagerStep(@NonNull PayloadManager payloadManager) {
    this.payloadManager = payloadManager;
  }

  @Then("parse payload successfully")
  public void parsePayloadSuccessfully() {
    PayloadKey key = new PayloadKey("person-profiles", "valid");
    assertThatNoException().isThrownBy(() -> this.payloadManager.getPayload(key));
  }
}
