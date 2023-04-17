package uk.gov.homeoffice.digital.sas.kafka.consumer.errorhandler;

import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_STOPPING_CONSUMING;

import io.micrometer.core.instrument.Counter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import uk.gov.homeoffice.digital.sas.kafka.exceptions.KafkaConsumerException;

@Component
@Slf4j
public class KafkaConsumerErrorHandler implements KafkaListenerErrorHandler {

  private Counter errorCounter;

  private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;


  public KafkaConsumerErrorHandler(KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry,
                                   Counter errorCounter) {
    this.kafkaListenerEndpointRegistry = kafkaListenerEndpointRegistry;
    this.errorCounter = errorCounter;
  }

  @Override
  public Object handleError(Message<?> message, ListenerExecutionFailedException exception) {
    // Need to throw only on chosen exception.
    if (exception.getCause() instanceof KafkaConsumerException) {
      errorCounter.increment();
      log.warn(KAFKA_STOPPING_CONSUMING, exception.getCause());
      kafkaListenerEndpointRegistry.stop();
    }

    throw exception;
  }

}
