package uk.gov.homeoffice.digital.sas.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.homeoffice.digital.sas.config.TestConfig;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaAction;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaEventMessage;
import uk.gov.homeoffice.digital.sas.kafka.producer.KafkaProducerService;
import uk.gov.homeoffice.digital.sas.kafka.validators.SchemaValidator;
import uk.gov.homeoffice.digital.sas.model.Profile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = TestConfig.class)
@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class KafkaConsumerServiceTest {

  private static final Long PROFILE_ID = 1L;
  private static final String TENANT_ID = "tenantId";
  private static final String PROFILE_NAME = "Original profile";

  private Profile profile;

  private String validMessage;

  @SpyBean
  private SchemaValidator schemaValidator;

  @Autowired
  private KafkaConsumerServiceImpl kafkaConsumerServiceImpl;

  @BeforeEach
  void setup() {
    profile = new Profile(null, TENANT_ID, PROFILE_NAME);
    profile.setId(PROFILE_ID);
    validMessage = "{\"schema\":\"uk.gov.homeoffice.digital.sas.model.Profile, 0.1.0\",\"resource\":{\"id\":\"c0a80018-870e-11b0-8187-0ea38cb30001\",\"tenantId\":\"00000000-0000-0000-0000-000000000000\",\"ownerId\":\"3343a960-de03-42ba-8769-767404fb2fcf\",\"timePeriodTypeId\":\"00000000-0000-0000-0000-000000000001\",\"shiftType\":null,\"actualStartTime\":1679456400000,\"actualEndTime\":1679457000000},\"action\":\"CREATE\"}";

  }

  @Test
  void should_returnTrue_when_correctMessage() {
    kafkaConsumerServiceImpl.consumer(validMessage);
    assertEquals(validMessage, kafkaConsumerServiceImpl.getMessage());
  }

  @Test
  void should_callIsSchemaValid()  {
    kafkaConsumerServiceImpl.consumer(validMessage);
    verify(schemaValidator).isSchemaValid(validMessage);
  }

}