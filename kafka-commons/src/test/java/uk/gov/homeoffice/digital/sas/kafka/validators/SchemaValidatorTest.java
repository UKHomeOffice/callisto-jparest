package uk.gov.homeoffice.digital.sas.kafka.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import uk.gov.homeoffice.digital.sas.config.TestConfig;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static uk.gov.homeoffice.digital.sas.Constants.TestConstants.*;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.*;

@SpringBootTest(classes = TestConfig.class)
@ExtendWith({OutputCaptureExtension.class})
class SchemaValidatorTest {


  @Value("${kafka.valid.schema.versions}")
  private List<String> validVersions;

  @Value("${kafka.resource.name}")
  private String resourceName;

  private String validMessage;
  private String invalidMessage;

  SchemaValidator schemaValidator;

  @BeforeEach
  void setup () {
    schemaValidator = new KafkaSchemaValidatorImpl();
    schemaValidator.setValidVersions(validVersions);
    schemaValidator.setResourceName(resourceName);
    validMessage = KAFKA_VALID_JSON_MESSAGE;
    invalidMessage = KAFKA_INVALID_JSON_MESSAGE;
  }

  @Test
  void should_returnTrue_when_schemaValid () {
    // given
    // when
    boolean actual1 = schemaValidator.isSchemaValid(this.validMessage);
    boolean actual2 = schemaValidator.isSchemaValid(this.invalidMessage);
    // then
    assertAll(
      () -> assertTrue(actual1),
      () -> assertFalse(actual2)
    );
  }

  @Test
  void should_returnCorrectLogMessage_when_validResource (CapturedOutput capturedOutput) {
    // given
    // when
    schemaValidator.isSchemaValid(validMessage);
    // then
    assertTrue(capturedOutput.getOut().contains(String.format(KAFKA_SCHEMA_VALIDATED, KAFKA_VALID_SCHEMA)));
  }

  @Test
  void should_returnCorrectLogMessage_invalidResource (CapturedOutput capturedOutput) {
    // given
    // when
    boolean actual = schemaValidator.isSchemaValid(KAFKA_INVALID_SCHEMA_RESOURCE_MESSAGE);
    // then
    assertTrue(capturedOutput.getOut().contains(String.format(KAFKA_SCHEMA_INVALID_RESOURCE, "uk.gov.homeoffice.digital.sas.timecard.model.TimeEntry")));
  }

  @Test
  void should_returnCorrectLogMessage_when_validVersion (CapturedOutput capturedOutput) {
    // given
    // when
    boolean actual = schemaValidator.isSchemaValid(validMessage);
    // then
    assertTrue(capturedOutput.getOut().contains(String.format(KAFKA_SCHEMA_VALIDATED, "uk.gov.homeoffice.digital.sas.model.Profile, 0.1.0")));
  }

  @Test
  void should_returnCorrectLogMessage_invalidVersion (CapturedOutput capturedOutput) {
    // given
    // when
    boolean actual = schemaValidator.isSchemaValid(invalidMessage);
    // then
    assertTrue(capturedOutput.getOut().contains(String.format(KAFKA_SCHEMA_INVALID_VERSION, "0.0.4")));
  }

  @Test
  void should_throwIllegalArgumentExceptionLogError_incorrectSchemaFormat(
      CapturedOutput capturedOutput) {
    // given
    // when
    boolean actual = schemaValidator.isSchemaValid(KAFKA_INVALID_SCHEMA_FORMAT_JSON_MESSAGE);
    // then
    assertTrue(capturedOutput.getOut().contains(String.format(KAFKA_SCHEMA_INCORRECT_FORMAT, "uk.gov"
        + ".homeoffice.digital.sas.model.Profile 0.0.4")));
  }
}