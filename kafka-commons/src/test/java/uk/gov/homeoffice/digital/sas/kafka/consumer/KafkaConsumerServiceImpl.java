package uk.gov.homeoffice.digital.sas.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.concurrent.CountDownLatch;
import lombok.Getter;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import uk.gov.homeoffice.digital.sas.kafka.validators.KafkaSchemaValidatorImpl;
import uk.gov.homeoffice.digital.sas.model.Profile;

@Service
@Getter
public class KafkaConsumerServiceImpl extends KafkaConsumerService<Profile> {

  private CountDownLatch latch = new CountDownLatch(1);

  protected KafkaConsumerServiceImpl(KafkaSchemaValidatorImpl schemaValidator) {
    super(schemaValidator);
  }

  @KafkaListener(
      topics = {"${spring.kafka.template.default-topic}"},
      groupId = "${spring.kafka.consumer.group-id}"
  )
  public void onMessage(@Payload String message) throws JsonProcessingException {
    if (latch != null) {
      latch.countDown();
      if (latch.getCount() == 0) {
        kafkaEventMessage = consume(message);
      }
    }
  }
}
