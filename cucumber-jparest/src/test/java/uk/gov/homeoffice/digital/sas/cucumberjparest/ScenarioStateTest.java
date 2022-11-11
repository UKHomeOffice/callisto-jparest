package uk.gov.homeoffice.digital.sas.cucumberjparest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ScenarioStateTest {

  private final ScenarioState scenarioState = new ScenarioState();

  private static Stream<String> serviceData() {
    return Stream.of("", null);
  }

  @ParameterizedTest
  @MethodSource("serviceData")
  void GIVEN_nullOrEmptyServiceName_WHEN_trackingService_THEN_throwAssertionError(String serviceName) {
    AssertionError thrown = assertThrows(
        AssertionError.class,
        () -> scenarioState.trackService(serviceName),
        "Expected createPayload() to throw an exception, but it didn't"
    );
    assertThat(
        thrown.getMessage(),
        is(
            "A service name must be provided because no service has previously been referenced."
        )
    );
  }
}