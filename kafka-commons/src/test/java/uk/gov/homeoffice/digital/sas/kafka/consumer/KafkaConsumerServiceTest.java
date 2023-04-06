package uk.gov.homeoffice.digital.sas.kafka.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.homeoffice.digital.sas.Constants.TestConstants.KAFKA_INVALID_VERSION;
import static uk.gov.homeoffice.digital.sas.Constants.TestConstants.KAFKA_VALID_VERSION;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_CONSUMING_MESSAGE;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_SCHEMA_INVALID_VERSION;

import java.util.concurrent.TimeUnit;

import org.apache.kafka.common.security.oauthbearer.secured.ValidateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import uk.gov.homeoffice.digital.sas.config.TestConfig;
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
  private static final int CONSUMER_TIMEOUT = 1;

  @Autowired
  private KafkaConsumerServiceImpl kafkaConsumerServiceImpl;

  KafkaEventMessage expectedKafkaEventMessage;

  @BeforeEach
  void setup() {
    Profile profile = new Profile(PROFILE_ID, TENANT_ID, PROFILE_NAME);
    expectedKafkaEventMessage =
        TestUtils.generateExpectedKafkaEventMessage(KAFKA_VALID_VERSION,
        profile,
        KafkaAction.CREATE);
  }

  //Does deserialize, logs success
  @Test
  void should_returnKafkaEventMessage_AndLogSuccess_when_correctMessage(CapturedOutput capturedOutput) {
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
  void should_returnNull_AndLogFailure_when_incorrectMessage(CapturedOutput capturedOutput) {
    kafkaConsumerServiceImpl.setExpectedNumberOfMessages(1);
    String message = TestUtils.createKafkaMessage(KAFKA_INVALID_VERSION);

    assertThrows(ValidateException.class, () ->
        kafkaConsumerServiceImpl.onMessage(message));

    assertThat(capturedOutput.getOut()).contains(String.format(KAFKA_SCHEMA_INVALID_VERSION,
        KAFKA_INVALID_VERSION));
  }
}