package uk.gov.homeoffice.digital.sas.kafka.consumer;

import java.util.concurrent.CountDownLatch;
import lombok.Getter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Basic Kafka consumer used to validate messages written to topics in integration tests
 */
@Component
@Getter
public class KafkaConsumer {
  private CountDownLatch latch = new CountDownLatch(1);
  private String payload;

  @KafkaListener(topics = "${spring.kafka.template.default-topic}")
  public void receive(ConsumerRecord<String, String> consumerRecord) {
    if (latch != null) {
      latch.countDown();
      if (latch.getCount() == 0) {
        payload = consumerRecord.value();
      }
    }
  }

  public void setExpectedNumberOfMessages(int expectedNumberOfMessages) {
    latch = new CountDownLatch(expectedNumberOfMessages);
  }
}
