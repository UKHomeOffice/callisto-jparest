package uk.gov.homeoffice.digital.sas.kafka.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import uk.gov.homeoffice.digital.sas.config.TestConfig;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

@SpringBootTest(classes = TestConfig.class)
@ExtendWith({OutputCaptureExtension.class})
class SchemaValidatorTest {

  @Autowired
  SchemaValidator schemaValidator;

  @Value("${kafka.valid.schema.versions}")
  private List<String> validVersions;

  @Value("${kafka.resource.name}")
  private String resourceName;

  private String validMessage;
  private String invalidMessage;

  @BeforeEach
  void setup () {
    schemaValidator.setValidVersions(validVersions);
    schemaValidator.setResourceName(resourceName);
    validMessage = "{\"schema\":\"uk.gov.homeoffice.digital.sas.model.Profile, 0.1.0\",\"resource\":{\"id\":\"c0a80018-870e-11b0-8187-0ea38cb30001\",\"tenantId\":\"00000000-0000-0000-0000-000000000000\",\"ownerId\":\"3343a960-de03-42ba-8769-767404fb2fcf\",\"timePeriodTypeId\":\"00000000-0000-0000-0000-000000000001\",\"shiftType\":null,\"actualStartTime\":1679456400000,\"actualEndTime\":1679457000000},\"action\":\"CREATE\"}";
    invalidMessage = "{\"schema\":\"uk.gov.homeoffice.digital.sas.model.Profile, 0.0.4\",\"resource\":{\"id\":\"c0a80018-870e-11b0-8187-0ea38cb30001\",\"tenantId\":\"00000000-0000-0000-0000-000000000000\",\"ownerId\":\"3343a960-de03-42ba-8769-767404fb2fcf\",\"timePeriodTypeId\":\"00000000-0000-0000-0000-000000000001\",\"shiftType\":null,\"actualStartTime\":1679456400000,\"actualEndTime\":1679457000000},\"action\":\"CREATE\"}";
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
    String messageValidResource = "{\"schema\":\"uk.gov.homeoffice.digital.sas.model.Profile, 0.1.0\",\"resource\":{\"id\":\"c0a80018-870e-11b0-8187-0ea38cb30001\",\"tenantId\":\"00000000-0000-0000-0000-000000000000\",\"ownerId\":\"3343a960-de03-42ba-8769-767404fb2fcf\",\"timePeriodTypeId\":\"00000000-0000-0000-0000-000000000001\",\"shiftType\":null,\"actualStartTime\":1679456400000,\"actualEndTime\":1679457000000},\"action\":\"CREATE\"}";
    // when
    schemaValidator.isSchemaValid(messageValidResource);
    // then
    assertTrue(capturedOutput.getOut().contains("Schema: [ uk.gov.homeoffice.digital.sas.model.Profile, 0.1.0 ] has been validated"));
  }

  @Test
  void should_returnCorrectLogMessage_invalidResource (CapturedOutput capturedOutput) {
    // given
    String messageValidResource = "{\"schema\":\"uk.gov.homeoffice.digital.sas.timecard.model.TimeEntry, 0.1.0\",\"resource\":{\"id\":\"c0a80018-870e-11b0-8187-0ea38cb30001\",\"tenantId\":\"00000000-0000-0000-0000-000000000000\",\"ownerId\":\"3343a960-de03-42ba-8769-767404fb2fcf\",\"timePeriodTypeId\":\"00000000-0000-0000-0000-000000000001\",\"shiftType\":null,\"actualStartTime\":1679456400000,\"actualEndTime\":1679457000000},\"action\":\"CREATE\"}";
    // when
    boolean actual = schemaValidator.isSchemaValid(messageValidResource);
    // then
    assertTrue(capturedOutput.getOut().contains("Invalid schema resource [ uk.gov.homeoffice.digital.sas.timecard.model.TimeEntry ]"));
  }

  @Test
  void should_returnCorrectLogMessage_when_validVersion (CapturedOutput capturedOutput) {
    // given
    // when
    boolean actual = schemaValidator.isSchemaValid(validMessage);
    // then
    assertTrue(capturedOutput.getOut().contains("Schema: [ uk.gov.homeoffice.digital.sas.model.Profile, 0.1.0 ] has been validated"));
  }

  @Test
  void should_returnCorrectLogMessage_invalidVersion (CapturedOutput capturedOutput) {
    // given
    // when
    boolean actual = schemaValidator.isSchemaValid(invalidMessage);
    // then
    assertTrue(capturedOutput.getOut().contains("Invalid schema version [ 0.0.4 ]"));
  }
}