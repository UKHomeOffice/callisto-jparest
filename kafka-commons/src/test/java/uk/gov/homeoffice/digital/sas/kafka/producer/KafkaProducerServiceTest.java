package uk.gov.homeoffice.digital.sas.kafka.producer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaAction;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaEventMessage;
import uk.gov.homeoffice.digital.sas.model.Profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static uk.gov.homeoffice.digital.sas.kafka.message.KafkaEventMessage.SCHEMA_FORMAT;

@ExtendWith(MockitoExtension.class)
class KafkaProducerServiceTest {

  private final static String TOPIC_NAME = "callisto-profile-topic";
  private final static String PROFILE_ID = "profileId";
  private final static String PROFILE_NAME = "profileX";
  private final static String SCHEMA_VERSION = "1.0.0";
  private Profile profile;

  @Captor
  private ArgumentCaptor<String> topicArgument;
  @Captor
  private ArgumentCaptor<String> messageKeyArgument;
  @Captor
  private ArgumentCaptor<KafkaEventMessage<Profile>> messageArgument;

  @Mock
  private KafkaTemplate<String, KafkaEventMessage<Profile>> kafkaTemplate;

  private KafkaProducerService<Profile> kafkaProducerService;

  @BeforeEach
  void setup() {
    profile = new Profile(PROFILE_ID, PROFILE_NAME);
    kafkaProducerService = new KafkaProducerService<>(kafkaTemplate, TOPIC_NAME, SCHEMA_VERSION);
  }

  @ParameterizedTest
  @EnumSource(value = KafkaAction.class, names = {"CREATE", "UPDATE", "DELETE"})
  void sendMessage_actionOnResource_messageIsSentWithCorrectArguments(KafkaAction action) {

    assertThatNoException().isThrownBy(() ->
        kafkaProducerService.sendMessage(PROFILE_ID, profile, action));

    Mockito.verify(kafkaTemplate)
        .send(topicArgument.capture(), messageKeyArgument.capture(), messageArgument.capture());

    assertThat(topicArgument.getValue()).isEqualTo(TOPIC_NAME);
    assertThat(messageKeyArgument.getValue()).isEqualTo(profile.getId());
    assertThat(messageArgument.getValue().getSchema()).isEqualTo(
        String.format(SCHEMA_FORMAT, Profile.class.getCanonicalName(), SCHEMA_VERSION));
    assertThat(messageArgument.getValue().getResource()).isEqualTo(profile);
    assertThat(messageArgument.getValue().getAction()).isEqualTo(action);
  }
}