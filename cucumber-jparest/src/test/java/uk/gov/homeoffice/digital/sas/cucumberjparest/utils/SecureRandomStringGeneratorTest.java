package uk.gov.homeoffice.digital.sas.cucumberjparest.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SecureRandomStringGeneratorTest {

  private static Stream<Arguments> stringToMapTestData() {
    return Stream.of(
        Arguments.of(10),
        Arguments.of(5),
        Arguments.of(0)
    );
  }

  @ParameterizedTest
  @MethodSource("stringToMapTestData")
  void randomAlphabetic_stringSizeProvided_generateRandomStringAsExpected(
      int stringSize) {
    String generatedString = SecureRandomStringGenerator.randomAlphabetic(stringSize);
    assertThat(generatedString).hasSize(stringSize)
        .matches("[a-zA-Z]{" + stringSize + "}");
  }
}