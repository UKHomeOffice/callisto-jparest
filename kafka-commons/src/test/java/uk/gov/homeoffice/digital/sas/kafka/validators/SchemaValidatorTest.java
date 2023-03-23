package uk.gov.homeoffice.digital.sas.kafka.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class SchemaValidatorTest {

  SchemaValidator schemaValidator = new SchemaValidator();

  private String validMessage;
  private String invalidMessage;

  @BeforeEach
  void setup () {
    validMessage = "{\"schema\":\"uk.gov.homeoffice.digital.sas.timecard.model.TimeEntry, 0.0.1\",\"resource\":{\"id\":\"c0a80018-870e-11b0-8187-0ea38cb30001\",\"tenantId\":\"00000000-0000-0000-0000-000000000000\",\"ownerId\":\"3343a960-de03-42ba-8769-767404fb2fcf\",\"timePeriodTypeId\":\"00000000-0000-0000-0000-000000000001\",\"shiftType\":null,\"actualStartTime\":1679456400000,\"actualEndTime\":1679457000000},\"action\":\"CREATE\"}";
    invalidMessage = "{\"schema\":\"uk.gov.homeoffice.digital.sas.timecard.model.TimeEntry, 0.0.4\",\"resource\":{\"id\":\"c0a80018-870e-11b0-8187-0ea38cb30001\",\"tenantId\":\"00000000-0000-0000-0000-000000000000\",\"ownerId\":\"3343a960-de03-42ba-8769-767404fb2fcf\",\"timePeriodTypeId\":\"00000000-0000-0000-0000-000000000001\",\"shiftType\":null,\"actualStartTime\":1679456400000,\"actualEndTime\":1679457000000},\"action\":\"CREATE\"}";
  }

  @Test
  void should_returnTrue_when_validSchema () throws IOException {
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
}