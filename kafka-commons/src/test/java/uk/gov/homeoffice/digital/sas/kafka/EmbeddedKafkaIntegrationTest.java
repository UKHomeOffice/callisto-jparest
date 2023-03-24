package uk.gov.homeoffice.digital.sas.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import uk.gov.homeoffice.digital.sas.config.TestConfig;
import uk.gov.homeoffice.digital.sas.kafka.consumer.KafkaConsumerServiceImpl;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaAction;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaEventMessage;
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

  private static final Long PROFILE_ID = 1L;
  private static final String TENANT_ID = "tenantId";
  private static final String PROFILE_NAME = "Original profile";
  private static final String UPDATED_PROFILE_NAME = "Updated profile";
  private static final int CONSUMER_TIMEOUT = 10;
  private Profile profile;

  @Value("${projectVersion}")
  private String version;

  @Autowired
  private KafkaProducerService<Profile> kafkaProducerService;

  @Autowired
  private KafkaConsumerServiceImpl kafkaConsumerServiceImpl;

  @Autowired
  private ProfileRepository profileRepository;

  @BeforeEach
  void setup() {
    profile = new Profile(null, TENANT_ID, PROFILE_NAME);
  }

  @Test
  void shouldSendCreateMessageToTopicFromProducer() throws Exception {
    profile.setId(PROFILE_ID);
    // GIVEN
    kafkaProducerService.sendMessage(PROFILE_ID.toString(), profile, KafkaAction.CREATE);

    // WHEN
    kafkaConsumerServiceImpl.getLatch().await(3, TimeUnit.SECONDS);
    String message = kafkaConsumerServiceImpl.receive();

    // THEN
    String expectedUpdateMessagePayload = generateExpectedPayload(version, profile,
        KafkaAction.CREATE);
    assertThat(message).isEqualTo(expectedUpdateMessagePayload);
    assertThat(kafkaConsumerServiceImpl.getKafkaEventMessage()).isNotNull();

    KafkaEventMessage expectedKafkaEventMessage = generateExpectedKafkaEventMessage(version,
        profile,
        KafkaAction.CREATE);

    isMessageDeserialized(expectedKafkaEventMessage);
  }

  //@Test
  //void shouldSendCreateMessageToTopicWhenProfileIsCreated() throws Exception {
  //  // GIVEN
  //  profileRepository.save(profile);
  //
  //  // WHEN
  //  boolean messageConsumed = kafkaConsumer.getLatch().await(CONSUMER_TIMEOUT, TimeUnit.SECONDS);
  //
  //  // THEN
  //  assertThat(messageConsumed).isTrue();
  //  String expectedUpdateMessagePayload = generateExpectedPayload(version, profile, KafkaAction.CREATE);
  //  assertThat(kafkaConsumer.getPayload()).isEqualTo(expectedUpdateMessagePayload);
  //}
  //
  //@Test
  //void shouldSendUpdateMessageToTopicWhenProfileIsUpdated() throws Exception {
  //  kafkaConsumer.setExpectedNumberOfMessages(2);
  //  // GIVEN
  //  profileRepository.saveAndFlush(profile);
  //  profile.setName(UPDATED_PROFILE_NAME);
  //  profile = profileRepository.saveAndFlush(profile);
  //
  //  // WHEN
  //  boolean messageConsumed = kafkaConsumer.getLatch().await(CONSUMER_TIMEOUT, TimeUnit.SECONDS);
  //
  //  // THEN
  //  assertThat(messageConsumed).isTrue();
  //  String expectedUpdateMessagePayload = generateExpectedPayload(version, profile, KafkaAction.UPDATE);
  //  assertThat(kafkaConsumer.getPayload()).isEqualTo(expectedUpdateMessagePayload);
  //}
  //
  //@Test
  //void shouldSendDeleteMessageToTopicWhenProfileIsDeleted() throws Exception {
  //  kafkaConsumer.setExpectedNumberOfMessages(2);
  //  // GIVEN
  //  profileRepository.save(profile);
  //  profileRepository.delete(profile);
  //
  //  // WHEN
  //  boolean messageConsumed = kafkaConsumer.getLatch().await(CONSUMER_TIMEOUT, TimeUnit.SECONDS);
  //
  //  // THEN
  //  assertThat(messageConsumed).isTrue();
  //  String expectedDeleteMessagePayload = generateExpectedPayload(version, profile, KafkaAction.DELETE);
  //  assertThat(kafkaConsumer.getPayload()).isEqualTo(expectedDeleteMessagePayload);
  //}

  private String generateExpectedPayload(String version, Profile profile, KafkaAction action) {
    return "{\"schema\":\""
        .concat(profile.getClass().getName())
        .concat(", ")
        .concat(version)
        .concat("\",\"resource\":{\"id\":")
        .concat(profile.getId().toString())
        .concat(",\"tenantId\":\"")
        .concat(profile.getTenantId())
        .concat("\",\"name\":\"")
        .concat(profile.getName())
        .concat("\"},\"action\":\"")
        .concat(action.name())
        .concat("\"}");
  }

  private KafkaEventMessage generateExpectedKafkaEventMessage(String version, Profile resource,
                                                              KafkaAction action) {
    return new KafkaEventMessage<>(version, resource, action);
  }

  private void isMessageDeserialized(KafkaEventMessage expectedKafkaEventMessage) {
    assertThat(kafkaConsumerServiceImpl.getKafkaEventMessage().getSchema().equals(expectedKafkaEventMessage.getSchema()));
    assertThat(kafkaConsumerServiceImpl.getKafkaEventMessage().getAction().equals(expectedKafkaEventMessage.getAction()));

    isResourceDeserialized();
  }



  private void isResourceDeserialized() {

    Profile actualProfile = getProfileAsConcreteType();
    assertAll(
        () -> assertThat(actualProfile.getId()).isEqualTo(profile.getId()),
        () -> assertThat(actualProfile.getName().equals(profile.getName())),
        () -> assertThat(actualProfile.getTenantId().equals(profile.getTenantId()))
    );
  }

  //This method is needed due to Jackson defaulting to LinkedHasMap on deserialization with
  // generic types. It converts the LinkedHaspMap to a concrete type.
  private Profile getProfileAsConcreteType() {
    ObjectMapper mapper = new ObjectMapper();
    Profile actualProfile = mapper.convertValue(
        kafkaConsumerServiceImpl.getKafkaEventMessage().getResource(), new TypeReference<>() {});
    return actualProfile;
  }


}
