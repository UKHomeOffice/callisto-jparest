package uk.gov.homeoffice.digital.sas.kafka.consumer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import lombok.Getter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaEventMessage;
import uk.gov.homeoffice.digital.sas.model.Profile;

@Service
public class KafkaConsumerServiceImpl implements ConsumerSeekAware {

  @Getter
  KafkaEventMessage<Profile> kafkaEventMessage;

  KafkaConsumerService<Profile> kafkaConsumerService;

  ConsumerSeekCallback callback;
  CountDownLatch latch;

  public KafkaConsumerServiceImpl(KafkaConsumerService<Profile> kafkaConsumerService) {
    this.kafkaConsumerService = kafkaConsumerService;
  }

  @Value("${spring.kafka.template.default-topic}")
  private String topic;

  @KafkaListener(topics = { "${spring.kafka.template.default-topic}" }, groupId = "${spring.kafka.consumer.group-id}")
  public void onMessage(@Payload String message) {
    if (latch == null) {
      throw new NullPointerException("Message recieved before the expected number of messages had been set." +
          " Ensure setExpectedNumberOfMessages has been called.");
    }
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

  public void setExpectedNumberOfMessages(int expectedNumberOfMessages) {
    latch = new CountDownLatch(expectedNumberOfMessages);
  }

  public boolean awaitMessages(long timeout, TimeUnit unit) {
    if (latch == null) {
      throw new NullPointerException("awaitMessages can not be called before the expected number" +
          " of messages has been set. Ensure setExpectedNumberOfMessages has been called.");
    }
    boolean latchCompleted = false;
    try {
      latchCompleted = latch.await(timeout, unit);
    } catch (InterruptedException ex) {
    }
    return latchCompleted;
  }
}
