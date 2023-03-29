package uk.gov.homeoffice.digital.sas.kafka.constants;

public final class Constants {

  private Constants() {
    throw new AssertionError();
  }

  //Log Messages
  public static final String KAFKA_TRANSACTION_INITIALIZED =
      "Kafka Transaction [ %s ] Initialized with message key [ %s ]";

  public static final String DATABASE_TRANSACTION_SUCCESSFUL =
      "Database transaction [ %s ] was successful";

  public static final String TRANSACTION_SUCCESSFUL =
      "Transaction successful with messageKey [ %s ]";

  public static final String DATABASE_TRANSACTION_FAILED =
      "Database transaction [ %s ] failed";

  public static final String KAFKA_FAILED_MESSAGE =
      "Message with key [ %s ] failed sending to topic [ %s ] action [ %s ]";

  public static final String KAFKA_SUCCESS_MESSAGE =
      "Message with key [ %s ] sent to topic [ %s ] with action [ %s ]";

  public static final String KAFKA_CONSUMING_MESSAGE =
      "Consuming message: [%s]";

  public static final String KAFKA_FAILED_DESERIALIZATION = "Message Failed to deserialize with "
      + "exception";

  public static final String KAFKA_SCHEMA_INCORRECT_FORMAT = "Schema has incorrect format: [ %s ]";

  public static final String KAFKA_SCHEMA_VALIDATED = "Schema: [ %s ] has been validated";

  public static final String KAFKA_SCHEMA_INVALID = "Schema: [ %s ] is invalid";

  public static final String KAFKA_SCHEMA_INVALID_RESOURCE = "Invalid schema resource [ %s ]";

  public static final String KAFKA_SCHEMA_INVALID_VERSION = "Invalid schema version [ %s ]";




  //Strings
  public static final String SCHEMA_JSON_ATTRIBUTE = "schema";

  public static final String SCHEMA_COMMA_DELIMETER = ", ";

  public static final String SCHEMA_FORMAT = "%s" + SCHEMA_COMMA_DELIMETER + "%s";
}
