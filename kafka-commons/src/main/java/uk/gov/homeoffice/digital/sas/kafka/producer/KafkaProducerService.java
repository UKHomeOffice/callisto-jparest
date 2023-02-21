package uk.gov.homeoffice.digital.sas.kafka.producer;

import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaAction;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaEventMessage;

@Service
@EnableAutoConfiguration
public class KafkaProducerService<T> {

  private final KafkaTemplate<String, KafkaEventMessage<T>> kafkaTemplate;
  private final String topicName;
  private final String projectVersion;

  public KafkaProducerService(
      KafkaTemplate<String, KafkaEventMessage<T>> kafkaTemplate,
      @Value("${spring.kafka.template.default-topic}") String topicName,
      @Value("${projectVersion}") String projectVersion) {
    this.kafkaTemplate = kafkaTemplate;
    this.topicName = topicName;
    this.projectVersion = projectVersion;
  }

  @SuppressWarnings("unchecked")
  public void sendMessage(@NotNull String messageKey, @NotNull T resource, KafkaAction action) {
    var kafkaEventMessage = new KafkaEventMessage<>(projectVersion,
        (Class<T>) resource.getClass(), resource, action);
    kafkaTemplate.send(topicName, messageKey, kafkaEventMessage);
  }
}
