package uk.gov.homeoffice.digital.sas.kafka.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.homeoffice.digital.sas.config.TestConfig;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.homeoffice.digital.sas.Constants.TestConstants.KAFKA_INVALID_SCHEMA_FORMAT_JSON_MESSAGE;
import static uk.gov.homeoffice.digital.sas.Constants.TestConstants.KAFKA_INVALID_VERSION;
import static uk.gov.homeoffice.digital.sas.Constants.TestConstants.KAFKA_JSON_MESSAGE;
import static uk.gov.homeoffice.digital.sas.Constants.TestConstants.KAFKA_VALID_RESOURCE;
import static uk.gov.homeoffice.digital.sas.Constants.TestConstants.KAFKA_VALID_VERSION;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_SCHEMA_INCORRECT_FORMAT;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_SCHEMA_INVALID;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_SCHEMA_INVALID_VERSION;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_SCHEMA_VALIDATED;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.SCHEMA_COMMA_DELIMETER;
import com.vdurmont.semver4j.Semver;

@SpringBootTest(classes = TestConfig.class)
@ExtendWith({OutputCaptureExtension.class})
class SchemaValidatorTest {

  SchemaValidator schemaValidator;

  @BeforeEach
  void setup () {
    schemaValidator = new KafkaSchemaValidatorImpl();
  }

  @Test
  void should_returnTrue_when_schemaValid() {
    // given
    ReflectionTestUtils.setField(schemaValidator, "supportedVersion", "^0.x.x");
    String message = String.format(KAFKA_JSON_MESSAGE, KAFKA_VALID_VERSION);
    // when
    boolean actual = schemaValidator.isSchemaValid(message);

    // then
    assertThat(actual).isTrue();
  }

  @Test
  void should_returnFalse_when_schemaInvalid() {
    //given
    ReflectionTestUtils.setField(schemaValidator, "supportedVersion", "^0.x.x");
    String message = String.format(KAFKA_JSON_MESSAGE, KAFKA_INVALID_VERSION);

    //when
    boolean actual = schemaValidator.isSchemaValid(message);

    //then
    assertThat(actual).isFalse();
  }

  @Test
  void should_returnCorrectLogMessage_when_validVersion(CapturedOutput capturedOutput) {
    // given
    ReflectionTestUtils.setField(schemaValidator, "supportedVersion", "^0.x.x");
    String message = String.format(KAFKA_JSON_MESSAGE, KAFKA_VALID_VERSION);
    // when
    schemaValidator.isSchemaValid(message);
    // then
    assertThat(capturedOutput.getOut()).contains(String.format(KAFKA_SCHEMA_VALIDATED, "uk.gov" +
        ".homeoffice.digital.sas.model.Profile, 0.1.0"));
  }

  @Test
  void should_returnCorrectLogMessage_when_invalidVersion(CapturedOutput capturedOutput) {
    // given
    ReflectionTestUtils.setField(schemaValidator, "supportedVersion", "^0.x.x");
    String message = String.format(KAFKA_JSON_MESSAGE, KAFKA_INVALID_VERSION);
    // when
    schemaValidator.isSchemaValid(message);
    // then
    assertThat(capturedOutput.getOut()).contains(String.format(KAFKA_SCHEMA_INVALID_VERSION,
        KAFKA_INVALID_VERSION));
  }

  @Test
  void should_returnLogError_when_incorrectSchemaFormat(
      CapturedOutput capturedOutput) {
    // given
    ReflectionTestUtils.setField(schemaValidator, "supportedVersion", "^0.x.x");
    String message = String.format(KAFKA_INVALID_SCHEMA_FORMAT_JSON_MESSAGE, KAFKA_VALID_VERSION);
    // when
    schemaValidator.isSchemaValid(String.format(message));
    // then
    assertThat(capturedOutput.getOut()).contains(String.format(KAFKA_SCHEMA_INCORRECT_FORMAT,
      KAFKA_VALID_RESOURCE + " " + KAFKA_VALID_VERSION));
  }

  @ParameterizedTest
  @CsvSource({"^1.x.x,2.0.0", "1.x.x,0.8.0", "1.x.x,2.0.0-SNAPSHOT", "^1.5.x,1.4.x"})
  void should_logError_when_VersionIsAboveSupportedVersion(String testSupportedVersion,
                                                                String messageVersion,
                                                                CapturedOutput capturedOutput) {
    //given
    ReflectionTestUtils.setField(schemaValidator, "supportedVersion", testSupportedVersion);

    String message = String.format(KAFKA_JSON_MESSAGE, messageVersion);
    //when
    schemaValidator.isSchemaValid(String.format(message));
    //then
    assertThat(capturedOutput.getOut()).contains(String.format(KAFKA_SCHEMA_INVALID_VERSION,
        messageVersion));
  }

  @ParameterizedTest
  @CsvSource({"^0.x.x,0.1.0", "^1.x.x,1.4.5", "^1.x.x,1.1.0", "^1.5.x,1.5.7",
      "^1.4.5,1.4.7-SNAPSHOT"})
  void should_logSuccess_when_versionIsBelowSupportedVersion(String testSupportedVersion,
                                                                  String messageVersion,
                                                                  CapturedOutput capturedOutput) {
    //given
    ReflectionTestUtils.setField(schemaValidator, "supportedVersion", testSupportedVersion);

    String message = String.format(KAFKA_JSON_MESSAGE, messageVersion);
    //when
    schemaValidator.isSchemaValid(String.format(message));

    //then
    String schema = KAFKA_VALID_RESOURCE + SCHEMA_COMMA_DELIMETER +  messageVersion;

    assertThat(capturedOutput.getOut()).contains(String.format(KAFKA_SCHEMA_VALIDATED,
        schema));
  }

  //anything up to v2

  //anything up to v3

  //

}