package uk.gov.homeoffice.digital.sas.Constants;

public class TestConstants {

  public static final String KAFKA_VALID_SCHEMA = "uk.gov.homeoffice.digital.sas.model.Profile,"
    + " 0.1.0";
  public static final String KAFKA_VALID_JSON_MESSAGE = "{\"schema\":\"uk.gov.homeoffice.digital"
    + ".sas.model.Profile, 0.1.0\",\"resource\":{\"id\":\"c0a80018-870e-11b0-8187-0ea38cb30001\",\"tenantId\":\"00000000-0000-0000-0000-000000000000\",\"ownerId\":\"3343a960-de03-42ba-8769-767404fb2fcf\",\"timePeriodTypeId\":\"00000000-0000-0000-0000-000000000001\",\"shiftType\":null,\"actualStartTime\":1679456400000,\"actualEndTime\":1679457000000},\"action\":\"CREATE\"}";

  public static final String KAFKA_INVALID_JSON_MESSAGE = "{\"schema\":\"uk.gov.homeoffice"
    + ".digital.sas.model.Profile, 0.0.4\",\"resource\":{\"id\":\"c0a80018-870e-11b0-8187-0ea38cb30001\",\"tenantId\":\"00000000-0000-0000-0000-000000000000\",\"ownerId\":\"3343a960-de03-42ba-8769-767404fb2fcf\",\"timePeriodTypeId\":\"00000000-0000-0000-0000-000000000001\",\"shiftType\":null,\"actualStartTime\":1679456400000,\"actualEndTime\":1679457000000},\"action\":\"CREATE\"}";

  public static final String KAFKA_INVALID_SCHEMA_FORMAT_JSON_MESSAGE = "{\"schema\":\"uk.gov"
    + ".homeoffice.digital.sas.model.Profile 0.0.4\",\"resource\":{\"id\":\"c0a80018-870e-11b0-8187-0ea38cb30001\",\"tenantId\":\"00000000-0000-0000-0000-000000000000\",\"ownerId\":\"3343a960-de03-42ba-8769-767404fb2fcf\",\"timePeriodTypeId\":\"00000000-0000-0000-0000-000000000001\",\"shiftType\":null,\"actualStartTime\":1679456400000,\"actualEndTime\":1679457000000},\"action\":\"CREATE\"}";

  public static final String KAFKA_INVALID_SCHEMA_RESOURCE_MESSAGE = "{\"schema\":\"uk.gov.homeoffice.digital.sas."
    + "timecard.model.TimeEntry, 0.1.0\",\"resource\":{\"id\":\"c0a80018-870e-11b0-8187-0ea38cb30001\",\"tenantId\":\"00000000-0000-0000-0000-000000000000\",\"ownerId\":\"3343a960-de03-42ba-8769-767404fb2fcf\",\"timePeriodTypeId\":\"00000000-0000-0000-0000-000000000001\",\"shiftType\":null,\"actualStartTime\":1679456400000,\"actualEndTime\":1679457000000},\"action\":\"CREATE\"}";

}
