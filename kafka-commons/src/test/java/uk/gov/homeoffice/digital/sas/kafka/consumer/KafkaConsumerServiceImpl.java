package uk.gov.homeoffice.digital.sas.kafka.consumer;

import java.util.concurrent.CountDownLatch;
import lombok.Getter;
import lombok.Setter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaEventMessage;
import uk.gov.homeoffice.digital.sas.model.Profile;

@Service
@Getter
@Setter
public class KafkaConsumerServiceImpl  implements ConsumerSeekAware {

  KafkaEventMessage<Profile> kafkaEventMessage;

  KafkaConsumerService<Profile> kafkaConsumerService;

  ConsumerSeekCallback callback;
  CountDownLatch latch;


  public KafkaConsumerServiceImpl(KafkaConsumerService<Profile> kafkaConsumerService) {
    this.kafkaConsumerService = kafkaConsumerService;
  }

  @Value("${spring.kafka.template.default-topic}")
  private String topic;

  @KafkaListener(
      topics = {"${spring.kafka.template.default-topic}"},
      groupId = "${spring.kafka.consumer.group-id}"
  )
  public void onMessage(@Payload String message) {
    if (latch.getCount() == 1) {
      kafkaEventMessage = kafkaConsumerService.consume(message);
    }
    latch.countDown();
  }

  @Override
  public void registerSeekCallback(ConsumerSeekCallback callback) {
    this.callback = callback;
  }
  
  public void TearDown() {
    latch = null;
    kafkaEventMessage = null;
    callback.seekToEnd(topic, 0);
  }
}
