package uk.gov.homeoffice.digital.sas.kafka.consumer;

import lombok.Getter;
import org.springframework.stereotype.Service;
import uk.gov.homeoffice.digital.sas.kafka.validators.SchemaValidator;
import uk.gov.homeoffice.digital.sas.model.Profile;

import java.util.concurrent.CountDownLatch;

@Service
@Getter
public class KafkaConsumerServiceImpl extends KafkaConsumerService<Profile> {

  private CountDownLatch latch = new CountDownLatch(1);

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
