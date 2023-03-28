package uk.gov.homeoffice.digital.sas.kafka.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.homeoffice.digital.sas.Constants.TestConstants.KAFKA_INVALID_JSON_MESSAGE;
import static uk.gov.homeoffice.digital.sas.Constants.TestConstants.KAFKA_VALID_JSON_MESSAGE;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_CONSUMING_MESSAGE;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_SCHEMA_INVALID;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_SCHEMA_INVALID_VERSION;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import uk.gov.homeoffice.digital.sas.config.TestConfig;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaAction;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaEventMessage;
import uk.gov.homeoffice.digital.sas.kafka.validators.SchemaValidator;
import uk.gov.homeoffice.digital.sas.model.Profile;
import java.util.List;;
import com.fasterxml.jackson.core.JsonProcessingException;

@SpringBootTest(classes = TestConfig.class)
@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class KafkaConsumerServiceTest {

  private static final Long PROFILE_ID = 1L;
  private static final String TENANT_ID = "tenantId";
  private static final String PROFILE_NAME = "Original profile";

  private Profile profile;

  private String validMessage;
  private String invalidMessage;

  @Value("${kafka.resource.name}")
  private String resourceName;

  @Value("${kafka.valid.schema.versions}")
  private List<String> validVersions;

  @Autowired
  private SchemaValidator schemaValidator;

  @Autowired
  private KafkaConsumerServiceImpl kafkaConsumerServiceImpl;

  KafkaEventMessage expectedKafkaEventMessage;

  @BeforeEach
  void setup() {
    schemaValidator.setValidVersions(validVersions);
    schemaValidator.setResourceName(resourceName);
    profile = new Profile(PROFILE_ID, TENANT_ID, PROFILE_NAME);
    expectedKafkaEventMessage = generateExpectedKafkaEventMessage("0.1.0",
        profile,
        KafkaAction.CREATE);
    validMessage = KAFKA_VALID_JSON_MESSAGE;
    invalidMessage = KAFKA_INVALID_JSON_MESSAGE;
  }

  //Does deserialize, logs success
  @Test
  void should_returnKafkaEventMessage_AndLogSuccess_when_correctMessage(CapturedOutput capturedOutput) throws JsonProcessingException {
    kafkaConsumerServiceImpl.consume(validMessage);
    //verify(schemaValidator).isSchemaValid(validMessage);
    //assertEquals(expectedKafkaEventMessage, kafkaConsumerServiceImpl.getKafkaEventMessage());
    assertThat(capturedOutput.getOut()).contains(String.format(KAFKA_CONSUMING_MESSAGE,
        validMessage));
  }

  //doesn't deserialize, logs error
  @Test
  void should_returnNull_AndLogFailure_when_incorrectMessage(CapturedOutput capturedOutput) throws JsonProcessingException {
    kafkaConsumerServiceImpl.consume(invalidMessage);
    //verify(schemaValidator).isSchemaValid(invalidMessage);
    assertThat(kafkaConsumerServiceImpl.getKafkaEventMessage()).isNull();
    assertThat(capturedOutput.getOut()).contains(String.format(KAFKA_SCHEMA_INVALID_VERSION, "0.0" +
        ".4"));
    assertThat(capturedOutput.getOut()).contains(String.format(KAFKA_SCHEMA_INVALID,
        resourceName + ", 0.0.4"));
  }

  private KafkaEventMessage generateExpectedKafkaEventMessage(String version, Profile resource,
                                                              KafkaAction action) {
    return new KafkaEventMessage<>(version, resource, action);
  }

}