package uk.gov.homeoffice.digital.sas.kafka.consumer;

import lombok.Getter;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaEventMessage;
import uk.gov.homeoffice.digital.sas.kafka.validators.SchemaValidator;
import uk.gov.homeoffice.digital.sas.model.Profile;

import java.util.concurrent.CountDownLatch;

@Service
@Getter
public class KafkaConsumerServiceImpl extends KafkaConsumerService<Profile> {

  private CountDownLatch latch = new CountDownLatch(1);

  public KafkaConsumerServiceImpl(SchemaValidator schemaValidator) {
    super(schemaValidator);
  }

  @KafkaListener(
      topics = {"${spring.kafka.template.default-topic}"},
      groupId = "${spring.kafka.consumer.group-id}"
  )
  public void onMessage(@Payload String message) {
    //if (latch != null) {
    //  latch.countDown();
    //  if (latch.getCount() == 0) {
        kafkaEventMessage = consumer(message);
    //  }
    //}
  }

  public void setExpectedNumberOfMessages(int expectedNumberOfMessages) {
    latch = new CountDownLatch(expectedNumberOfMessages);
  }
}
