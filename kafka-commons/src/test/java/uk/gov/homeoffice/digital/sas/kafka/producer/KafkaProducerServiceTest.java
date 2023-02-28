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
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaAction;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaEventMessage;
import uk.gov.homeoffice.digital.sas.model.Profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_FAILED_MESSAGE;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_SUCCESS_MESSAGE;
import static uk.gov.homeoffice.digital.sas.kafka.message.KafkaEventMessage.SCHEMA_FORMAT;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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

  @Mock
  private CompletableFuture<SendResult<String, KafkaEventMessage<Profile>>> responseFuture;

  private KafkaProducerService<Profile> kafkaProducerService;

  @BeforeEach
  void setup() {
    profile = new Profile(PROFILE_ID, PROFILE_NAME);
    kafkaProducerService = new KafkaProducerService<>(kafkaTemplate, TOPIC_NAME, SCHEMA_VERSION);
  }

  @ParameterizedTest
  @EnumSource(value = KafkaAction.class)
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

  @ParameterizedTest
  @EnumSource(value = KafkaAction.class)
  void sendMessage_actionOnResource_onFailureInterruptLogged(KafkaAction action)
      throws ExecutionException, InterruptedException {
    ListAppender<ILoggingEvent> listAppender = getLoggingEventListAppender();

    List<ILoggingEvent> logList = listAppender.list;

    responseFuture = mock(CompletableFuture.class);

    when(kafkaTemplate.send(any(), any(), any()))
        .thenReturn(responseFuture);
    Mockito.doThrow(InterruptedException.class).when(responseFuture).get();

    kafkaProducerService.sendMessage(PROFILE_ID, profile, action);
    assertThat(responseFuture.isDone()).isFalse();
    assertThat(String.format(
        KAFKA_FAILED_MESSAGE,
        PROFILE_ID, "callisto-profile-topic",action.toString().toLowerCase())).isEqualTo(logList.get(0).getMessage());
  }

  @ParameterizedTest
  @EnumSource(value = KafkaAction.class)
  void sendMessage_actionOnResource_onFailureMessageLogged(KafkaAction action)
      throws ExecutionException, InterruptedException {
    ListAppender<ILoggingEvent> listAppender = getLoggingEventListAppender();

    List<ILoggingEvent> logList = listAppender.list;

    responseFuture = mock(CompletableFuture.class);

    when(kafkaTemplate.send(any(), any(), any()))
        .thenReturn(responseFuture);
    Mockito.doThrow(ExecutionException.class).when(responseFuture).get();

    kafkaProducerService.sendMessage(PROFILE_ID, profile, action);
    assertThat(responseFuture.isDone()).isFalse();
    assertThat(String.format(
        KAFKA_FAILED_MESSAGE,
        PROFILE_ID, "callisto-profile-topic",action.toString().toLowerCase())).isEqualTo(logList.get(0).getMessage());
  }

  @ParameterizedTest
  @EnumSource(value = KafkaAction.class)
  void sendMessage_actionOnResource_onSuccessMessageLogged(KafkaAction action) throws InterruptedException, ExecutionException {
    ListAppender<ILoggingEvent> listAppender = getLoggingEventListAppender();

    List<ILoggingEvent> logList = listAppender.list;

    responseFuture = spy(CompletableFuture.class);
    SendResult<String, KafkaEventMessage<Profile>> sendResult = mock(SendResult.class);

    when(kafkaTemplate.send(any(), any(), any())).thenReturn(responseFuture);
    when(responseFuture.complete(sendResult)).thenReturn(true);
    assertThat(responseFuture.isDone()).isTrue();

    kafkaProducerService.sendMessage(PROFILE_ID, profile,
        action);

    assertThat(String.format(
        KAFKA_SUCCESS_MESSAGE,
        PROFILE_ID, "callisto-profile-topic",
        action.toString().toLowerCase())).isEqualTo(logList.get(0).getMessage());

  }

  private static ListAppender<ILoggingEvent> getLoggingEventListAppender() {
    Logger kafkaLogger = (Logger) LoggerFactory.getLogger(KafkaProducerService.class);

    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();

    kafkaLogger.addAppender(listAppender);
    return listAppender;
  }
}