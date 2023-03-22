package uk.gov.homeoffice.digital.sas.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaEventMessage;

@Slf4j
public abstract class KafkaConsumerService<T> {

  protected KafkaEventMessage<T> kafkaEventMessage;

  @KafkaListener(
      topics = "${spring.kafka.template.default-topic}",
      groupId = "${spring.kafka.consumer.group-id}")
  public void consumer(@Payload String message
  ) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    KafkaEventMessage kafkaEventMessage = mapper.readValue(message, KafkaEventMessage.class);
    log.info("consuming message =" + message);
  }

}
