package uk.gov.homeoffice.digital.sas.kafka.consumer;

import lombok.Getter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaEventMessage;
import uk.gov.homeoffice.digital.sas.model.Profile;

import java.util.List;
import java.util.concurrent.CountDownLatch;

@Service
public class KafkaConsumerServiceImpl extends KafkaConsumerService<Profile> {

  private CountDownLatch latch = new CountDownLatch(1);

  public KafkaConsumerServiceImpl(@Value("${kafka.resource.name}") String resourceName,
                                  @Value("${kafka.valid.schema.versions}") List<String> validVersions) {
    super(resourceName, validVersions);
  }

  public String receive() {
    if (latch != null) {
      latch.countDown();
      if (latch.getCount() == 0) {
        return this.getMessage();
      }
    }
    return null;
  }
}
