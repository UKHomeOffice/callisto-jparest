package uk.gov.homeoffice.digital.sas.cucumberjparest;

import static org.assertj.core.api.Assertions.assertThat;

public class ScenarioState {

  private String service;

  @SuppressWarnings("squid:S5960")// Assertions are needed in this test library
  public String trackService(String service) {
    if (service != null && !service.isEmpty()) {
      this.service = service;
    } else {
      service = this.service;
    }

    assertThat(service)
      .withFailMessage(
          "A service name must be provided because no service has previously been referenced.")
        .isNotEmpty();
    return service;
  }
}
