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

  //Consumer logs

  public static final String KAFKA_CONSUMING_MESSAGE =
      "Consuming message: [%s]";

  public static final String KAFKA_SCHEMA_VALIDATED = "Schema: [ %s ] has been validated";

  public static final String KAFKA_SCHEMA_INCORRECT_FORMAT = "Schema has incorrect format: [ %s ]";

  public static final String KAFKA_SCHEMA_INVALID_VERSION = "Invalid schema version [ %s ]";

  public static final String KAFKA_RESOURCE_NOT_UNDERSTOOD =
      "The resource within the schema is not understood [ %s ]";

  public static final String KAFKA_PAYLOAD_IS_NULL = "Received payload is null";

  public static final String RESOURCE_TYPE_IS_NULL = "Resource type is null";

  public static final String KAFKA_COULD_NOT_DESERIALIZE_RESOURCE =
      "The resource recieved could not be deserialized [ %s ]";

  public static final String KAFKA_DESERIALIZATION_TO_CONCRETE_TYPE_FAILED =
      "Failed deserialization on message [ %s ]";

  public static final String KAFKA_STOPPING_CONSUMING = "Stopping consumer due to critical error";

  public static final String KAFKA_SUCCESSFUL_DESERIALIZATION =
      "Successful deserialization of message entity [ %s ] created";

  //Strings
  public static final String SCHEMA_JSON_ATTRIBUTE = "schema";
  public static final String RESOURCE_JSON_ATTRIBUTE = "resource";

  public static final String SCHEMA_COMMA_DELIMETER = ", ";

  public static final String SCHEMA_FORMAT = "%s" + SCHEMA_COMMA_DELIMETER + "%s";
}
