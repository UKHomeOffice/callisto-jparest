package uk.gov.homeoffice.digital.sas.kafka.consumer.configuration;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("uk.gov.homeoffice.digital.sas.kafka.consumer")
public class KafkaConsumerConfig {

  @Bean
  public Counter consumerErrorCounter(MeterRegistry meterRegistry,
                                      @Value("${actuator.kafka.failure.url}")
                                      String actuatorKafkaFailureUrl,
                                      @Value("${actuator.error.type}")
                                      String actuatorErrorType,
                                      @Value("${actuator.failure.description}")
                                      String actuatorKafkaFailureDescription
                                      ) {
    return Counter.builder(actuatorKafkaFailureUrl)
        .tag("type", actuatorErrorType)
        .description(actuatorKafkaFailureDescription)
        .register(meterRegistry);
  }
}
