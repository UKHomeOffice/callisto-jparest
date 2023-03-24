package uk.gov.homeoffice.digital.sas.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.homeoffice.digital.sas.config.TestConfig;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaAction;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaEventMessage;
import uk.gov.homeoffice.digital.sas.kafka.producer.KafkaProducerService;
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(
  partitions = 1,
  brokerProperties = {
    "listeners=PLAINTEXT://localhost:3333",
    "port=3333"
  }
)
@Slf4j
class KafkaConsumerServiceTest {

  private static final Long PROFILE_ID = 1L;
  private static final String TENANT_ID = "tenantId";
  private static final String PROFILE_NAME = "Original profile";

  private Profile profile;

  @Autowired
  private KafkaProducerService<Profile> kafkaProducerService;

  @Autowired
  private KafkaConsumerServiceImpl kafkaConsumerServiceImpl;

  @BeforeEach
  void setup() {
    profile = new Profile(null, TENANT_ID, PROFILE_NAME);
  }

  @Test
  void should_producerMessage() throws InterruptedException, IOException {
    profile.setId(PROFILE_ID);

    kafkaProducerService.sendMessage(PROFILE_ID.toString(), profile, KafkaAction.CREATE);

    boolean consumed = kafkaConsumerServiceImpl.getLatch().await(3, TimeUnit.SECONDS);

  }



}