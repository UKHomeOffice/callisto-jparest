package uk.gov.homeoffice.digital.sas.kafka.validators;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import uk.gov.homeoffice.digital.sas.config.TestConfig;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaAction;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.homeoffice.digital.sas.Constants.TestConstants.KAFKA_INVALID_RESOURCE;
import static uk.gov.homeoffice.digital.sas.Constants.TestConstants.KAFKA_INVALID_SCHEMA_FORMAT_JSON_MESSAGE;
import static uk.gov.homeoffice.digital.sas.Constants.TestConstants.KAFKA_INVALID_VERSION;
import static uk.gov.homeoffice.digital.sas.Constants.TestConstants.KAFKA_JSON_MESSAGE;
import static uk.gov.homeoffice.digital.sas.Constants.TestConstants.KAFKA_VALID_RESOURCE;
import static uk.gov.homeoffice.digital.sas.Constants.TestConstants.KAFKA_VALID_VERSION;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_SCHEMA_INVALID;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_SCHEMA_INVALID_RESOURCE;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_SCHEMA_INVALID_VERSION;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_SCHEMA_VALIDATED;

@SpringBootTest(classes = TestConfig.class)
@ExtendWith({OutputCaptureExtension.class})
class SchemaValidatorTest {

  private DefaultArtifactVersion supportedVersion;

  @Value("${kafka.resource.name}")
  private String resourceName;

  SchemaValidator schemaValidator;

  @BeforeEach
  void setup () {
    schemaValidator = new KafkaSchemaValidatorImpl();
    schemaValidator.setResourceName(resourceName);
  }

  @Test
  void should_returnTrue_when_schemaValid() {
    // given
    supportedVersion = new DefaultArtifactVersion("0.1.0");
    schemaValidator.setSupportedVersion(supportedVersion);

    String message = String.format(KAFKA_JSON_MESSAGE, KAFKA_VALID_RESOURCE,
        KAFKA_VALID_VERSION);
    // when
    boolean actual = schemaValidator.isSchemaValid(message);

    // then
    assertThat(actual).isTrue();
  }

  @Test
  void should_returnFalse_when_schemaInvalid() {
    //given
    supportedVersion = new DefaultArtifactVersion("0.1.0");
    schemaValidator.setSupportedVersion(supportedVersion);

    String message = String.format(KAFKA_JSON_MESSAGE, KAFKA_INVALID_RESOURCE,
        KAFKA_INVALID_VERSION);

    //when
    boolean actual = schemaValidator.isSchemaValid(message);

    //then
    assertThat(actual).isFalse();
  }

  @Test
  void should_returnCorrectLogMessage_when_validResource(CapturedOutput capturedOutput) {
    // given
    supportedVersion = new DefaultArtifactVersion("0.1.0");
    schemaValidator.setSupportedVersion(supportedVersion);

    String message = String.format(KAFKA_JSON_MESSAGE, KAFKA_VALID_RESOURCE,
        KAFKA_VALID_VERSION);
    // when
    schemaValidator.isSchemaValid(message);

    // then
    assertThat(capturedOutput.getOut()).contains(String.format(KAFKA_SCHEMA_VALIDATED,
        KAFKA_VALID_RESOURCE + ", " + KAFKA_VALID_VERSION));
  }

  @Test
  void should_returnCorrectLogMessage_when_invalidResource(CapturedOutput capturedOutput) {
    // given
    supportedVersion = new DefaultArtifactVersion("0.1.0");
    schemaValidator.setSupportedVersion(supportedVersion);

    String message = String.format(KAFKA_JSON_MESSAGE, KAFKA_INVALID_RESOURCE,
        KAFKA_VALID_VERSION);
    // when
    schemaValidator.isSchemaValid(message);
    // then
    assertThat(capturedOutput.getOut()).contains(String.format(KAFKA_SCHEMA_INVALID_RESOURCE, "uk" +
        ".gov.homeoffice.digital.sas.timecard.model.TimeEntry"));
  }

  @Test
  void should_returnCorrectLogMessage_when_validVersion(CapturedOutput capturedOutput) {
    // given
    supportedVersion = new DefaultArtifactVersion("0.1.0");
    schemaValidator.setSupportedVersion(supportedVersion);

    String message = String.format(KAFKA_JSON_MESSAGE, KAFKA_VALID_RESOURCE,
        KAFKA_VALID_VERSION);
    // when
    schemaValidator.isSchemaValid(message);
    // then
    assertThat(capturedOutput.getOut()).contains(String.format(KAFKA_SCHEMA_VALIDATED, "uk.gov" +
        ".homeoffice.digital.sas.model.Profile, 0.1.0"));
  }

  @Test
  void should_returnCorrectLogMessage_when_invalidVersion(CapturedOutput capturedOutput) {
    // given
    supportedVersion = new DefaultArtifactVersion("0.1.0");
    schemaValidator.setSupportedVersion(supportedVersion);

    String message = String.format(KAFKA_JSON_MESSAGE, KAFKA_VALID_RESOURCE,
        KAFKA_INVALID_VERSION);
    // when
    schemaValidator.isSchemaValid(message);
    // then
    assertThat(capturedOutput.getOut()).contains(String.format(KAFKA_SCHEMA_INVALID_VERSION,
        "0.2.0"));
  }

  @Test
  void should_returnLogError_when_incorrectSchemaFormat(
      CapturedOutput capturedOutput) {
    // given
    supportedVersion = new DefaultArtifactVersion("0.1.0");
    schemaValidator.setSupportedVersion(supportedVersion);

    String message = String.format(KAFKA_INVALID_SCHEMA_FORMAT_JSON_MESSAGE, KAFKA_VALID_RESOURCE,
        KAFKA_VALID_VERSION);
    // when
    schemaValidator.isSchemaValid(String.format(message));
    // then
    assertThat(capturedOutput.getOut()).contains(String.format(KAFKA_SCHEMA_INVALID,
      KAFKA_VALID_RESOURCE + " " + KAFKA_VALID_VERSION));
  }

  //when major version is above current supported version
  @ParameterizedTest
  @CsvSource({"1.0.0,2.0.0", "1.5.0,1.8.0", "1.5.7, 4.5.7"})
  void should_logError_when_VersionIsAboveSupportedVersion(String testSupportedVersion,
                                                                String messageVersion,
                                                                CapturedOutput capturedOutput) {
    //given
    supportedVersion = new DefaultArtifactVersion(testSupportedVersion);
    schemaValidator.setSupportedVersion(supportedVersion);

    String message = String.format(KAFKA_JSON_MESSAGE, KAFKA_VALID_RESOURCE, messageVersion);
    //when
    schemaValidator.isSchemaValid(String.format(message));
    //then
    assertThat(capturedOutput.getOut()).contains(String.format(KAFKA_SCHEMA_INVALID_VERSION,
        messageVersion));
  }

  @ParameterizedTest
  @CsvSource({"1.5.0,1.4.0", "2.0.0,1.0.0", "1.0.0,0.4.0", "1.5.0,0.4.0", "3.8.6, 3.7.2"})
  void should_logSuccess_when_versionIsBelowSupportedVersion(String testSupportedVersion,
                                                                  String messageVersion,
                                                                  CapturedOutput capturedOutput) {
    //given
    supportedVersion = new DefaultArtifactVersion(testSupportedVersion);
    schemaValidator.setSupportedVersion(supportedVersion);

    String message = String.format(KAFKA_JSON_MESSAGE, KAFKA_VALID_RESOURCE, messageVersion);
    //when
    schemaValidator.isSchemaValid(String.format(message));
    //then
    assertThat(capturedOutput.getOut()).contains(String.format(KAFKA_SCHEMA_VALIDATED,
        messageVersion));
  }

}