package uk.gov.homeoffice.digital.sas.cucumberjparest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.homeoffice.digital.sas.cucumberjparest.scenarios.ScenarioState;

class ScenarioStateTest {

  private final ScenarioState scenarioState = new ScenarioState();

  private static Stream<String> serviceData() {
    return Stream.of("", null);
  }

  @ParameterizedTest
  @MethodSource("serviceData")
  void trackService_nullOrEmptyServiceNameProvided_throwsAssertionError(String serviceName) {
    AssertionError thrown = assertThrows(
        AssertionError.class,
        () -> scenarioState.trackService(serviceName),
        "Expected createPayload() to throw an exception, but it didn't"
    );
    assertThat(
        thrown.getMessage())
        .isEqualTo(
            "A service name must be provided because no service has previously been referenced."
        );
  }
}