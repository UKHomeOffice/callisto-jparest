package uk.gov.homeoffice.digital.sas.kafka.exceptions;

import org.apache.kafka.common.KafkaException;

public class KafkaConsumerException extends KafkaException {

  public KafkaConsumerException(String message) {
    super(message);
  }

  public KafkaConsumerException(String message, Throwable cause) {
    super(message, cause);
  }
}
