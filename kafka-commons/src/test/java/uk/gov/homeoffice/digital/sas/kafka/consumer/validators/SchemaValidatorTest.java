package uk.gov.homeoffice.digital.sas.kafka.consumer.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import uk.gov.homeoffice.digital.sas.utils.TestUtils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.homeoffice.digital.sas.Constants.TestConstants.KAFKA_INVALID_VERSION;
import static uk.gov.homeoffice.digital.sas.Constants.TestConstants.KAFKA_VALID_SCHEMA_RESOURCE;
import static uk.gov.homeoffice.digital.sas.Constants.TestConstants.KAFKA_VALID_VERSION;
import static uk.gov.homeoffice.digital.sas.Constants.TestConstants.SCHEMA_FIELD_INVALID_VALUE;
import static uk.gov.homeoffice.digital.sas.Constants.TestConstants.SCHEMA_FIELD_VALUE;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_SCHEMA_INCORRECT_FORMAT;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_SCHEMA_INVALID_VERSION;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_SCHEMA_VALIDATED;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.SCHEMA_COMMA_DELIMETER;

@ExtendWith({OutputCaptureExtension.class})
class SchemaValidatorTest {

  private static final String SEMVER_VERSION_IDENTIFIER = "^0.x.x";

  @Test
  void should_returnTrue_when_schemaValid() {
    // given
    SchemaValidator schemaValidator = new SchemaValidator(SEMVER_VERSION_IDENTIFIER);
    String message = TestUtils.createKafkaMessage(KAFKA_VALID_VERSION);
    // when
    boolean actual = schemaValidator.isSchemaValid(message);

    // then
    assertThat(actual).isTrue();
  }

  @Test
  void should_returnFalse_when_schemaInvalid() {
    //given
    SchemaValidator schemaValidator = new SchemaValidator(SEMVER_VERSION_IDENTIFIER);
    String message = TestUtils.createKafkaMessage(KAFKA_INVALID_VERSION);

    //when
    boolean actual = schemaValidator.isSchemaValid(message);

    //then
    assertThat(actual).isFalse();
  }

  @Test
  void should_returnCorrectLogMessage_when_validVersion(CapturedOutput capturedOutput) {
    // given
    SchemaValidator schemaValidator = new SchemaValidator(SEMVER_VERSION_IDENTIFIER);
    String message = TestUtils.createKafkaMessage(KAFKA_VALID_VERSION);
    // when
    schemaValidator.isSchemaValid(message);
    // then
    assertThat(capturedOutput.getOut()).contains(String.format(KAFKA_SCHEMA_VALIDATED,
        String.format(SCHEMA_FIELD_VALUE, KAFKA_VALID_VERSION)));
  }

  @Test
  void should_returnCorrectLogMessage_when_invalidVersion(CapturedOutput capturedOutput) {
    // given
    SchemaValidator schemaValidator = new SchemaValidator(SEMVER_VERSION_IDENTIFIER);
    String message = TestUtils.createKafkaMessage(KAFKA_INVALID_VERSION);
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
    SchemaValidator schemaValidator = new SchemaValidator(SEMVER_VERSION_IDENTIFIER);
    String message = TestUtils.createKafkaMessage(SCHEMA_FIELD_INVALID_VALUE, KAFKA_VALID_VERSION);
    // when
    schemaValidator.isSchemaValid(message);
    // then
    assertThat(capturedOutput.getOut()).contains(String.format(KAFKA_SCHEMA_INCORRECT_FORMAT,
      KAFKA_VALID_SCHEMA_RESOURCE + " " + KAFKA_VALID_VERSION));
  }

  @ParameterizedTest
  @CsvSource({"^1.x.x,2.0.0", "1.x.x,0.8.0", "1.x.x,2.0.0-SNAPSHOT", "^1.5.x,1.4.x"})
  void isSchemaValid_versionIsNotSupported_logsError(String testSupportedVersion,
                                                                String messageVersion,
                                                                CapturedOutput capturedOutput) {
    //given
    SchemaValidator schemaValidator = new SchemaValidator(testSupportedVersion);

    String message = TestUtils.createKafkaMessage(messageVersion);
    //when
    schemaValidator.isSchemaValid(message);
    //then
    assertThat(capturedOutput.getOut()).contains(String.format(KAFKA_SCHEMA_INVALID_VERSION,
        messageVersion));
  }

  @ParameterizedTest
  @CsvSource({"^0.x.x,0.1.0", "^1.x.x,1.4.5", "^1.x.x,1.1.0", "^1.5.x,1.5.7",
      "^1.4.5,1.4.7-SNAPSHOT"})
  void isSchemaValid_versionIsSupported_logsSuccess(String testSupportedVersion,
                                                                  String messageVersion,
                                                                  CapturedOutput capturedOutput) {
    //given
    SchemaValidator schemaValidator = new SchemaValidator(testSupportedVersion);

    String message = TestUtils.createKafkaMessage(messageVersion);
    //when
    schemaValidator.isSchemaValid(message);

    //then
    String schema = KAFKA_VALID_SCHEMA_RESOURCE + SCHEMA_COMMA_DELIMETER +  messageVersion;

    assertThat(capturedOutput.getOut()).contains(String.format(KAFKA_SCHEMA_VALIDATED,
        schema));
  }
}