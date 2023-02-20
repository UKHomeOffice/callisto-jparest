package uk.gov.homeoffice.digital.sas.kafka.consumer;

import java.util.concurrent.CountDownLatch;
import lombok.Getter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import java.util.logging.Logger;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Basic Kafka consumer used to validate messages written to topics in integration tests
 */
@Component
@Getter
public class KafkaConsumer {

  private final static Logger LOGGER = Logger.getLogger(KafkaConsumer.class.getName());
  private final CountDownLatch latch = new CountDownLatch(1);
  private String payload;

  @KafkaListener(topics = "${spring.kafka.template.default-topic}")
  public void receive(ConsumerRecord<String, String> consumerRecord) {
    payload = consumerRecord.value();
    LOGGER.info("Payload received.. ");
    LOGGER.info("Key: ");
    LOGGER.info(consumerRecord.key());
    LOGGER.info("Value: ");
    LOGGER.info(consumerRecord.value());
    latch.countDown();
  }
}