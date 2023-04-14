package uk.gov.homeoffice.digital.sas.kafka.configuration;

import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_STOPPING_CONSUMING;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.stereotype.Component;
import uk.gov.homeoffice.digital.sas.kafka.exceptions.KafkaConsumerException;

@Component
@Slf4j
public class KafkaErrorHandler {

  private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

  private Counter errorCounter;

  private final MeterRegistry meterRegistry;

  public KafkaErrorHandler(KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry,
                           @Value("${actuator.kafka.failure.url}") String actuatorKafkaFailureUrl,
                           @Value("${actuator.error.type}") String actuatorErrorType,
                           @Value("${actuator.failure.description}")
                             String actuatorKafkaFailureDescription,
                           MeterRegistry meterRegistry) {
    this.kafkaListenerEndpointRegistry = kafkaListenerEndpointRegistry;
    this.meterRegistry = meterRegistry;
    errorCounter = setUpCounters(actuatorKafkaFailureUrl, actuatorErrorType,
        actuatorKafkaFailureDescription);
  }

  @Bean(value = "kafkaConsumerErrorHandler")
  public KafkaListenerErrorHandler errorHandler() {
    return (message, exception) -> {

      // Need to throw only on chosen exception.
      if (exception.getCause() instanceof KafkaConsumerException) {
        errorCounter.increment();
        log.warn(KAFKA_STOPPING_CONSUMING, exception.getCause());
        kafkaListenerEndpointRegistry.stop();
      }
      throw exception;
    };
  }

  private Counter setUpCounters(String endpointUrl, String type, String description) {
    return Counter.builder(endpointUrl)
        .tag("type", type)
        .description(description)
        .register(meterRegistry);
  }
}
