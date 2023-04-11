package uk.gov.homeoffice.digital.sas.kafka.producer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
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
import static org.mockito.Mockito.when;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_FAILED_MESSAGE;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_SUCCESS_MESSAGE;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.SCHEMA_FORMAT;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@ExtendWith(MockitoExtension.class)
class KafkaProducerServiceTest {

  private final static String TOPIC_NAME = "callisto-profile-topic";
  private final static Long PROFILE_ID = 1L;

  private final static String TENANT_ID = "tenantId";
  private final static String PROFILE_NAME = "profileX";
  private final static String SCHEMA_VERSION = "1.0.0";

  private final static Date START_TIME = Date.from(Instant.parse("2022-01-01T15:00:00"));
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
  private CompletableFuture<SendResult<String, KafkaEventMessage<Profile>>> responseFutureMock;
  @Spy
  private CompletableFuture<SendResult<String, KafkaEventMessage<Profile>>> responseFutureSpy;
  @Mock
  SendResult<String, KafkaEventMessage<Profile>> sendResult;

  private KafkaProducerService<Profile> kafkaProducerService;

  @BeforeEach
  void setup() {
    profile = new Profile(PROFILE_ID, TENANT_ID, PROFILE_NAME, START_TIME);
    kafkaProducerService = new KafkaProducerService<>(kafkaTemplate, TOPIC_NAME, SCHEMA_VERSION);
  }

  @ParameterizedTest
  @EnumSource(value = KafkaAction.class)
  void sendMessage_actionOnResource_messageIsSentWithCorrectArguments(KafkaAction action) {
    when(kafkaTemplate.send(any(), any(), any()))
        .thenReturn(responseFutureMock);

    assertThatNoException().isThrownBy(() ->
        kafkaProducerService.sendMessage(PROFILE_ID.toString(), profile, action));

    Mockito.verify(kafkaTemplate)
        .send(topicArgument.capture(), messageKeyArgument.capture(), messageArgument.capture());

    assertThat(topicArgument.getValue()).isEqualTo(TOPIC_NAME);
    assertThat(messageKeyArgument.getValue()).isEqualTo(profile.getId().toString());
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

    when(kafkaTemplate.send(any(), any(), any()))
        .thenReturn(responseFutureMock);
    Mockito.doThrow(InterruptedException.class).when(responseFutureMock).get();

    kafkaProducerService.sendMessage(PROFILE_ID.toString(), profile, action);
    assertThat(responseFutureMock.isDone()).isFalse();
    assertThat(logList.get(0).getMessage()).isEqualTo(String.format(
        KAFKA_FAILED_MESSAGE,
        PROFILE_ID, "callisto-profile-topic",action.toString().toLowerCase()));
  }

  @ParameterizedTest
  @EnumSource(value = KafkaAction.class)
  void sendMessage_actionOnResource_onFailureMessageLogged(KafkaAction action)
      throws ExecutionException, InterruptedException {
    ListAppender<ILoggingEvent> listAppender = getLoggingEventListAppender();

    List<ILoggingEvent> logList = listAppender.list;

    when(kafkaTemplate.send(any(), any(), any()))
        .thenReturn(responseFutureMock);
    Mockito.doThrow(ExecutionException.class).when(responseFutureMock).get();

    kafkaProducerService.sendMessage(PROFILE_ID.toString(), profile, action);
    assertThat(responseFutureMock.isDone()).isFalse();
    assertThat(logList.get(0).getMessage()).isEqualTo(
        String.format(
            KAFKA_FAILED_MESSAGE,
            PROFILE_ID, "callisto-profile-topic",action.toString().toLowerCase())
    );
  }

  @ParameterizedTest
  @EnumSource(value = KafkaAction.class)
  void sendMessage_actionOnResource_onSuccessMessageLogged(KafkaAction action) {
    ListAppender<ILoggingEvent> listAppender = getLoggingEventListAppender();

    List<ILoggingEvent> logList = listAppender.list;

    when(kafkaTemplate.send(any(), any(), any())).thenReturn(responseFutureSpy);
    when(responseFutureSpy.complete(sendResult)).thenReturn(true);
    assertThat(responseFutureSpy.isDone()).isTrue();

    kafkaProducerService.sendMessage(PROFILE_ID.toString(), profile,
        action);

    assertThat(logList.get(0).getMessage()).isEqualTo(String.format(
        KAFKA_SUCCESS_MESSAGE,
        PROFILE_ID, "callisto-profile-topic",action.toString().toLowerCase()));
  }

  private static ListAppender<ILoggingEvent> getLoggingEventListAppender() {
    Logger kafkaLogger = (Logger) LoggerFactory.getLogger(KafkaProducerService.class);

    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();

    kafkaLogger.addAppender(listAppender);
    return listAppender;
  }
}