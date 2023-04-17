package uk.gov.homeoffice.digital.sas.kafka.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.homeoffice.digital.sas.Constants.TestConstants.KAFKA_VALID_VERSION;
import static uk.gov.homeoffice.digital.sas.Constants.TestConstants.SCHEMA_FIELD_VALUE;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.RESOURCE_JSON_ATTRIBUTE;
import static uk.gov.homeoffice.digital.sas.kafka.consumer.KafkaConsumerUtils.getResourceFromMessageAsString;
import static uk.gov.homeoffice.digital.sas.kafka.consumer.KafkaConsumerUtils.getSchemaFromMessageAsString;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.homeoffice.digital.sas.config.TestConfig;
import uk.gov.homeoffice.digital.sas.utils.TestUtils;

@SpringBootTest(classes = TestConfig.class)
class KafkaConsumerUtilsTest {

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