package uk.gov.homeoffice.digital.sas.kafka.consumer;

import java.util.concurrent.CountDownLatch;
import lombok.Getter;
import lombok.Setter;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaEventMessage;
import uk.gov.homeoffice.digital.sas.model.Profile;

@Service
@Getter
@Setter
public class KafkaConsumerServiceImpl {

  KafkaEventMessage<Profile> kafkaEventMessage;

  KafkaConsumerService<Profile> kafkaConsumerService;

  public KafkaConsumerServiceImpl(KafkaConsumerService<Profile> kafkaConsumerService) {
    this.kafkaConsumerService = kafkaConsumerService;
  }

  private CountDownLatch latch = new CountDownLatch(1);

  @KafkaListener(
      topics = {"${spring.kafka.template.default-topic}"},
      groupId = "${spring.kafka.consumer.group-id}"
  )
  public void onMessage(@Payload String message) {
    kafkaEventMessage = kafkaConsumerService.consume(message);
    latch.countDown();
  }
}
