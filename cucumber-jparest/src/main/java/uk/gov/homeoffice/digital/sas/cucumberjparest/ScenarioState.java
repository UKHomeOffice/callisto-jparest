package uk.gov.homeoffice.digital.sas.cucumberjparest;

import static org.assertj.core.api.Assertions.assertThat;

public class ScenarioState {

  private String service;

  @SuppressWarnings("squid:S5960")// Assertions are needed in this test library
  public String trackService(String serviceName) {
    if (serviceName != null && !serviceName.isEmpty()) {
      this.service = serviceName;
    } else {
      serviceName = this.service;
    }

    assertThat(serviceName)
      .withFailMessage(
          "A service name must be provided because no service has previously been referenced.")
        .isNotEmpty();
    return serviceName;
  }
}
