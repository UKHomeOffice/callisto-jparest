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
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import uk.gov.homeoffice.digital.sas.TestConfig;
import uk.gov.homeoffice.digital.sas.kafka.consumer.KafkaConsumer;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaAction;
import uk.gov.homeoffice.digital.sas.kafka.producer.KafkaProducerService;
import uk.gov.homeoffice.digital.sas.model.Profile;
import uk.gov.homeoffice.digital.sas.repository.ProfileRepository;

@SpringBootTest(classes = TestConfig.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(
    partitions = 1,
    brokerProperties = {
        "listeners=PLAINTEXT://localhost:3333",
        "port=3333"
    }
)
class EmbeddedKafkaIntegrationTest {

  private static final String PROFILE_ID = "profileId";
  private static final String PROFILE_NAME = "Original profile";
  private static final String UPDATED_PROFILE_NAME = "Updated profile";
  private static final int CONSUMER_TIMEOUT = 10;
  private String EXPECTED_CREATE_MESSAGE_PAYLOAD;
  private Profile profile;

  @Value("${projectVersion}")
  private String version;

  @Autowired
  private KafkaProducerService<Profile> kafkaProducerService;

  @Autowired
  private KafkaConsumer kafkaConsumer;

  @Autowired
  private ProfileRepository profileRepository;

  @BeforeEach
  void setup() {
    profile = new Profile(PROFILE_ID, PROFILE_NAME);
    EXPECTED_CREATE_MESSAGE_PAYLOAD = generatePayload(version, profile, KafkaAction.CREATE);
  }

  @Test
  void shouldSendCreateMessageToTopicFromProducer() throws Exception{
    kafkaProducerService.sendMessage(profile.getId(), Profile.class, profile, KafkaAction.CREATE);

    boolean messageConsumed = kafkaConsumer.getLatch().await(CONSUMER_TIMEOUT, TimeUnit.SECONDS);
    assertThat(messageConsumed).isTrue();
    assertThat(kafkaConsumer.getPayload()).isEqualTo(EXPECTED_CREATE_MESSAGE_PAYLOAD);
  }

  @Test
  void shouldSendCreateMessageToTopicWhenProfileIsCreated() throws Exception {
    profileRepository.save(profile);
    boolean messageConsumed = kafkaConsumer.getLatch().await(CONSUMER_TIMEOUT, TimeUnit.SECONDS);
    assertThat(messageConsumed).isTrue();

    assertThat(kafkaConsumer.getPayload()).isEqualTo(EXPECTED_CREATE_MESSAGE_PAYLOAD);
  }

  @Test
  void shouldSendUpdateMessageToTopicWhenProfileIsUpdated() throws Exception {
    profileRepository.save(profile);
    profile.setName(UPDATED_PROFILE_NAME);
    profile = profileRepository.save(profile);

    boolean messageConsumed = kafkaConsumer.getLatch().await(CONSUMER_TIMEOUT, TimeUnit.SECONDS);
    assertThat(messageConsumed).isTrue();

    String expectedUpdateMessagePayload = generatePayload(version, profile, KafkaAction.UPDATE);
    assertThat(kafkaConsumer.getPayload()).isEqualTo(expectedUpdateMessagePayload);
  }

  @Test
  void shouldSendDeleteMessageToTopicWhenProfileIsDeleted() throws Exception {
    profileRepository.save(profile);
    profileRepository.delete(profile);

    boolean messageConsumed = kafkaConsumer.getLatch().await(CONSUMER_TIMEOUT, TimeUnit.SECONDS);
    assertThat(messageConsumed).isTrue();

    String expectedDeleteMessagePayload = generatePayload(version, profile, KafkaAction.DELETE);
    assertThat(kafkaConsumer.getPayload()).isEqualTo(expectedDeleteMessagePayload);
  }

  private String generatePayload(String version, Profile profile, KafkaAction action) {
    return "{\"schema\":\"uk.gov.homeoffice.digital.sas.model.Profile, "
        .concat(version)
        .concat("\",\"resource\":{\"id\":\"")
        .concat(profile.getId())
        .concat("\",\"name\":\"")
        .concat(profile.getName())
        .concat("\"},\"action\":\"")
        .concat(action.name())
        .concat("\"}");
  }
}
