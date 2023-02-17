package uk.gov.homeoffice.digital.sas.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.homeoffice.digital.sas.TestConfig;
import uk.gov.homeoffice.digital.sas.kafka.consumer.KafkaConsumer;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaAction;
import uk.gov.homeoffice.digital.sas.kafka.producer.KafkaProducerService;
import uk.gov.homeoffice.digital.sas.model.Profile;

@SpringBootTest(classes = TestConfig.class)
@DirtiesContext
@EmbeddedKafka(
    partitions = 1,
    brokerProperties = {
        "listeners=PLAINTEXT://localhost:3333",
        "port=3333"
    }
)
class EmbeddedKafkaIntegrationTest {

  private final static String PROFILE_ID = "profileId";
  private String EXPECTED_READ_PAYLOAD;
  private Profile profile;

  @Value("${projectVersion}")
  private String version;

  @Autowired
  private KafkaProducerService<Profile> kafkaProducerService;

  @Autowired
  private KafkaConsumer kafkaConsumer;

  @BeforeEach
  void setup() {
    profile = new Profile(PROFILE_ID);
    EXPECTED_READ_PAYLOAD = "{\"schema\":\"uk.gov.homeoffice.digital.sas.model.Profile, "
        .concat(this.version)
        .concat("\",\"resource\":{\"id\":\"")
        .concat(profile.getId())
        .concat("\"},\"action\":\"")
        .concat(KafkaAction.CREATE.name())
        .concat("\"}");
  }

  @Test
  void shouldSendProfileToTopicFromProducer() throws Exception{
    kafkaProducerService.sendMessage(profile.getId(), Profile.class, profile, KafkaAction.CREATE);

    boolean messageConsumed = kafkaConsumer.getLatch().await(10, TimeUnit.SECONDS);
    assertThat(messageConsumed).isTrue();
    assertThat(kafkaConsumer.getPayload()).isEqualTo(EXPECTED_READ_PAYLOAD);
  }
}
