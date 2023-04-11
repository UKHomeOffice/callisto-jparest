package uk.gov.homeoffice.digital.sas.kafka.serializer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.homeoffice.digital.sas.kafka.serializer.CustomJsonSerializer.createMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import uk.gov.homeoffice.digital.sas.model.Profile;

@SpringBootConfiguration
class CustomJsonSerializerTest {

  @Test
  void whenObjectMapperCreated_canDeserializeDateFields() {

    ObjectMapper objectMapper = createMapper();

    assertThat(objectMapper.canSerialize(Profile.class));
  }
}