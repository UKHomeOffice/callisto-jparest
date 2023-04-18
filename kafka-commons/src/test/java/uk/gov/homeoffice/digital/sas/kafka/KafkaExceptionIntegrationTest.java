package uk.gov.homeoffice.digital.sas.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_STOPPING_CONSUMING;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import uk.gov.homeoffice.digital.sas.config.TestConfigWithJpa;
import uk.gov.homeoffice.digital.sas.kafka.consumer.KafkaConsumerServiceImpl;
import uk.gov.homeoffice.digital.sas.model.Profile;
import uk.gov.homeoffice.digital.sas.repository.ProfileRepository;
import uk.gov.homeoffice.digital.sas.utils.TestUtils;

/**
 *This is seperated from the EmbeddedKafkaIntegrationTest class, due to the difficulty of overriding
 * the schemaVersion property in an individual test.
 *
 */

@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(classes = TestConfigWithJpa.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(
    partitions = 1
)
@TestPropertySource(properties = {
    "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "schemaVersion=1.0.0"})
class KafkaExceptionIntegrationTest {

  private static final Long PROFILE_ID = 1L;
  private static final String TENANT_ID = "tenantId";
  private static final String PROFILE_NAME = "Original profile";

  private final static Date START_TIME = TestUtils.getAsDate(LocalDateTime.now());
  private static final int CONSUMER_TIMEOUT = 3;
  private Profile profile;

  @Autowired
  KafkaConsumerServiceImpl kafkaConsumerServiceImpl;

  @Autowired
  private ProfileRepository profileRepository;

  @BeforeEach
  void setup() {
    profile = new Profile(PROFILE_ID, TENANT_ID, PROFILE_NAME, START_TIME);
  }

  @AfterEach
  void cleanup() {
    kafkaConsumerServiceImpl.tearDown();
  }

  /**
   * Invalid schema version is set with the @TestPropertySources annotation at class level
   */

  @Test
  void givenMessageReceivedWithInvalidSchemaVersion_consumerShouldStopListening(CapturedOutput capturedOutput) {
    kafkaConsumerServiceImpl.setExpectedNumberOfMessages(1);

    profileRepository.save(profile);

    assertThat(kafkaConsumerServiceImpl.awaitMessages(CONSUMER_TIMEOUT, TimeUnit.SECONDS)).isFalse();

    assertThat(capturedOutput.getOut()).contains(KAFKA_STOPPING_CONSUMING);

  }

}
