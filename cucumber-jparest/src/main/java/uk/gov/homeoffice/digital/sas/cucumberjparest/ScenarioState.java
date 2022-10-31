package uk.gov.homeoffice.digital.sas.cucumberjparest;

import static org.assertj.core.api.Assertions.assertThat;

public class ScenarioState {

  private String service;

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
