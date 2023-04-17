package uk.gov.homeoffice.digital.sas.kafka.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.homeoffice.digital.sas.Constants.TestConstants.KAFKA_VALID_VERSION;
import static uk.gov.homeoffice.digital.sas.Constants.TestConstants.SCHEMA_FIELD_VALUE;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_DESERIALIZATION_TO_CONCRETE_TYPE_FAILED;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_SUCCESSFUL_DESERIALIZATION;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.RESOURCE_JSON_ATTRIBUTE;
import static uk.gov.homeoffice.digital.sas.kafka.consumer.KafkaConsumerUtils.getResourceFromMessageAsString;
import static uk.gov.homeoffice.digital.sas.kafka.consumer.KafkaConsumerUtils.getSchemaFromMessageAsString;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.time.LocalDateTime;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.util.ObjectUtils;
import uk.gov.homeoffice.digital.sas.config.TestConfig;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaAction;
import uk.gov.homeoffice.digital.sas.model.Profile;
import uk.gov.homeoffice.digital.sas.utils.TestUtils;

@SpringBootTest(classes = TestConfig.class)
@ExtendWith(OutputCaptureExtension.class)
class KafkaConsumerUtilsTest {

  private static final Long PROFILE_ID = 1L;
  private static final String TENANT_ID = "tenantId";
  private static final String PROFILE_NAME = "Original profile";
  private final static Date START_TIME = TestUtils.getAsDate(LocalDateTime.now());

  @Test
  void getSchemaFromMessageAsString_returnsSchemaAsResource() {
    String payload = TestUtils.createKafkaMessage(KAFKA_VALID_VERSION);

    assertThat(getSchemaFromMessageAsString(payload))
        .isEqualTo(String.format(SCHEMA_FIELD_VALUE, KAFKA_VALID_VERSION));

  }

  @Test
  void getResourceFromMessageAsString_returnMessageAsString() {
    String payload = TestUtils.createKafkaMessage(KAFKA_VALID_VERSION);

    JsonObject jsonMessage = JsonParser.parseString(payload).getAsJsonObject();
    jsonMessage.get(RESOURCE_JSON_ATTRIBUTE).toString();

    assertThat(getResourceFromMessageAsString(payload))
        .isEqualTo(jsonMessage.get(RESOURCE_JSON_ATTRIBUTE).toString());
  }

}