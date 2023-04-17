package uk.gov.homeoffice.digital.sas.kafka.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.homeoffice.digital.sas.Constants.TestConstants.KAFKA_INVALID_SCHEMA_RESOURCE;
import static uk.gov.homeoffice.digital.sas.Constants.TestConstants.KAFKA_INVALID_VERSION;
import static uk.gov.homeoffice.digital.sas.Constants.TestConstants.KAFKA_VALID_VERSION;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_CONSUMING_MESSAGE;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_DESERIALIZATION_TO_CONCRETE_TYPE_FAILED;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_SCHEMA_INVALID_VERSION;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_STOPPING_CONSUMING;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_SUCCESSFUL_DESERIALIZATION;
import static uk.gov.homeoffice.digital.sas.kafka.consumer.KafkaConsumerUtils.getSchemaFromMessageAsString;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import uk.gov.homeoffice.digital.sas.config.TestConfig;
import uk.gov.homeoffice.digital.sas.kafka.exceptions.KafkaConsumerException;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaAction;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaEventMessage;
import uk.gov.homeoffice.digital.sas.model.Profile;
import uk.gov.homeoffice.digital.sas.utils.TestUtils;

@SpringBootTest(classes = TestConfig.class)
@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class KafkaConsumerServiceTest {

  private static final Long PROFILE_ID = 1L;
  private static final String TENANT_ID = "tenantId";
  private static final String PROFILE_NAME = "Original profile";
  private final static Date START_TIME = TestUtils.getAsDate(LocalDateTime.now());
  private static final int CONSUMER_TIMEOUT = 1;

  @Autowired
  private KafkaConsumerServiceImpl kafkaConsumerServiceImpl;

  @Autowired
  private KafkaConsumerService kafkaConsumerService;

  KafkaEventMessage expectedKafkaEventMessage;

  @BeforeEach
  void setup() {
    Profile profile = new Profile(PROFILE_ID, TENANT_ID, PROFILE_NAME, START_TIME);
    expectedKafkaEventMessage =
        TestUtils.generateExpectedKafkaEventMessage(KAFKA_VALID_VERSION,
        profile,
        KafkaAction.CREATE);
  }

  //Does deserialize, logs success
  @Test
  void should_returnKafkaEventMessage_AndLogSuccess_when_correctMessage(CapturedOutput capturedOutput) throws JsonProcessingException {
    kafkaConsumerServiceImpl.setExpectedNumberOfMessages(1);
    String message = TestUtils.createKafkaMessage(KAFKA_VALID_VERSION);

    kafkaConsumerServiceImpl.onMessage(message);

    assertThat(kafkaConsumerServiceImpl.awaitMessages(CONSUMER_TIMEOUT, TimeUnit.SECONDS)).isTrue();
    assertThat(kafkaConsumerServiceImpl.getKafkaEventMessage()).isNotNull();
    assertThat(kafkaConsumerServiceImpl.getKafkaEventMessage().getSchema()).isEqualTo(expectedKafkaEventMessage.getSchema());
    assertThat(kafkaConsumerServiceImpl.getKafkaEventMessage().getAction()).isEqualTo(expectedKafkaEventMessage.getAction());
    assertThat(capturedOutput.getOut()).contains(String.format(KAFKA_CONSUMING_MESSAGE, message));
  }

  //doesn't deserialize, logs error
  @Test
  void should_returnNull_AndLogFailure_when_incorrectMessage() {
    kafkaConsumerServiceImpl.setExpectedNumberOfMessages(1);
    String message = TestUtils.createKafkaMessage(KAFKA_INVALID_VERSION);

    assertThatThrownBy(() -> {
      kafkaConsumerServiceImpl.onMessage(message);
    }).isInstanceOf(KafkaConsumerException.class)
        .hasMessageContaining(String.format(KAFKA_SCHEMA_INVALID_VERSION,
            getSchemaFromMessageAsString(message)));
  }

  @Test
  void checkDeserializedResource_logsSuccessWhenValidPayload(CapturedOutput capturedOutput) {
    Profile profile = new Profile(PROFILE_ID, TENANT_ID, PROFILE_NAME, START_TIME);
    kafkaConsumerService.checkDeserializedResource("resource", profile);

    assertThat(capturedOutput.getOut()).contains(String.format(KAFKA_SUCCESSFUL_DESERIALIZATION,
        "resource"));
  }

  @Test
  void checkDeserializedResource_logsFailureWhenInvalidPayload(CapturedOutput capturedOutput) {
    Profile profile = null;
    kafkaConsumerService.checkDeserializedResource("resource", profile);

    assertThat(capturedOutput.getOut()).contains(String.format(KAFKA_DESERIALIZATION_TO_CONCRETE_TYPE_FAILED,
        "resource"));
  }

  @Test
  void isResourceOfType_returnsTrueWhenValidSchema() {
    String payload = TestUtils.createKafkaMessage(KAFKA_VALID_VERSION);
    assertThat(kafkaConsumerService.isResourceOfType(payload, Profile.class)).isTrue();
  }

  @Test
  void isResourceOfType_returnFalseWhenInvalidSchema() {
    String payload = TestUtils.createKafkaMessage(KAFKA_INVALID_SCHEMA_RESOURCE, KAFKA_VALID_VERSION);
    assertThat(kafkaConsumerService.isResourceOfType(payload, Profile.class)).isFalse();
  }
}