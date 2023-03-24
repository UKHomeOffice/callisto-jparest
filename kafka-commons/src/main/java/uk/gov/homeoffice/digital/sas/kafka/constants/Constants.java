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

  public static final String SCHEMA = "schema";
}
