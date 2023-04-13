package uk.gov.homeoffice.digital.sas.kafka.configuration;

import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_STOPPING_CONSUMING;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.stereotype.Component;
import uk.gov.homeoffice.digital.sas.kafka.exceptions.KafkaConsumerException;


@Component
@Slf4j
public class KafkaConsumerConfig {

  private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

  public KafkaConsumerConfig(KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry) {
    this.kafkaListenerEndpointRegistry = kafkaListenerEndpointRegistry;
  }

  @Bean(value = "kafkaConsumerErrorHandler")
  public KafkaListenerErrorHandler errorHandler() {
    return (message, exception) -> {

      // Need to throw only on chosen exception.
      if (exception.getCause() instanceof KafkaConsumerException) {
        log.warn(KAFKA_STOPPING_CONSUMING, exception.getCause());
        kafkaListenerEndpointRegistry.stop();
      }
      throw exception;
    };
  }
}
