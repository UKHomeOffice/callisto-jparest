package uk.gov.homeoffice.digital.sas.kafka.consumer;

import lombok.Getter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import uk.gov.homeoffice.digital.sas.model.Profile;

import java.util.List;
import java.util.concurrent.CountDownLatch;

@Component
@Getter
public class KafkaConsumerServiceImpl extends KafkaConsumerService<Profile> {

  private CountDownLatch latch = new CountDownLatch(1);
  private String payload;

  public KafkaConsumerServiceImpl(@Value("${kafka.resource.name}") String resourceName,
                                  @Value("${kafka.valid.schema.versions}") List<String> validVersions) {
    super(resourceName, validVersions);
  }

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
