package uk.gov.homeoffice.digital.sas.kafka.serializer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.homeoffice.digital.sas.kafka.serializer.CustomJsonSerializer.createMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import uk.gov.homeoffice.digital.sas.model.Profile;
import uk.gov.homeoffice.digital.sas.utils.TestUtils;

@SpringBootConfiguration
class CustomJsonSerializerTest {

  private static final Long PROFILE_ID = 1L;
  private static final String TENANT_ID = "tenantId";
  private static final String PROFILE_NAME = "Original profile";
  private final static Date START_TIME = TestUtils.getAsDate(LocalDateTime.of(
      2022, 04, 17, 01, 30));

  private Profile profile;

  ObjectMapper objectMapper = createMapper();

  @Test
  void whenObjectMapperCreated_canDeserializeDateFields() {


    assertThat(objectMapper.canSerialize(Profile.class));
  }

  @Test
  void whenSerializing_dateIsInIso8601Format() throws JsonProcessingException {
    profile = new Profile(PROFILE_ID, TENANT_ID, PROFILE_NAME, START_TIME);

    String jsonString = objectMapper.writeValueAsString(profile);

    JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();

    String startTime = jsonObject.get("startTime").getAsString();

    assertThat(startTime).isNotNull();
    assertThat(startTime).isEqualTo("2022-04-17T01:30:00.000+00:00");
  }
}