package uk.gov.homeoffice.digital.sas.cucumberjparest.utils;

import static org.junit.jupiter.api.Assertions.*;

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
  void GIVEN_string_size_WHEN_generating_random_string_THEN_produces_the_expected_string(
      int stringSize) {
    String generatedString = SecureRandomStringGenerator.randomAlphabetic(stringSize);
    assertEquals(generatedString.length(), stringSize);
    assertTrue(generatedString.matches("[a-zA-Z]{" + stringSize + "}"));
  }
}