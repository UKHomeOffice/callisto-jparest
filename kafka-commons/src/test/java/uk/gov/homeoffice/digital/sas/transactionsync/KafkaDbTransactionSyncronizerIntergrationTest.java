package uk.gov.homeoffice.digital.sas.transactionsync;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.DATABASE_TRANSACTION_SUCCESSFUL;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_TRANSACTION_INITIALIZED;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.TRANSACTION_SUCCESSFUL;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import uk.gov.homeoffice.digital.sas.config.TestConfig;
import uk.gov.homeoffice.digital.sas.kafka.transactionsync.KafkaDbTransactionSynchronizer;
import uk.gov.homeoffice.digital.sas.model.Profile;
import uk.gov.homeoffice.digital.sas.repository.ProfileRepository;

@SpringBootTest(classes = TestConfig.class)
@DirtiesContext
@EmbeddedKafka(
    partitions = 1,
    brokerProperties = {
        "listeners=PLAINTEXT://localhost:3333",
        "port=3333"
    }
)
class KafkaDbTransactionSyncronizerIntergrationTest {
  private static final Long PROFILE_ID = new Random().nextLong();
  private static final String PROFILE_NAME = "Original profile";
  private Profile profile;

  private static ObjectMapper mapper = new ObjectMapper();

  @Autowired
  private ProfileRepository profileRepository;

  @BeforeEach
  void setup() {
    profile = new Profile(String.valueOf(PROFILE_ID), PROFILE_NAME);
  }

  @Test
  void givenValidRequest_WhenSendingCreateRequest_thenTransactionSyncLogsSuccessMessage()
      throws Exception {
    ListAppender<ILoggingEvent> listAppender = getLoggingEventListAppender();

    List<ILoggingEvent> logList = listAppender.list;

    profileRepository.save(profile);

    assertThat(String.format(
        KAFKA_TRANSACTION_INITIALIZED,
        "create" , PROFILE_ID)).isEqualTo(logList.get(0).getMessage());

    assertThat(logList.get(1).getMessage()).isEqualTo(String.format(
        DATABASE_TRANSACTION_SUCCESSFUL, "create"));
    assertThat(logList.get(2).getMessage()).isEqualTo(String.format(
        TRANSACTION_SUCCESSFUL, PROFILE_ID));

  }

  @Test
  void givenValidRequest_WhenSendingUpdateRequest_thenTransactionSyncLogsSuccessMessage()
      throws Exception {
    ListAppender<ILoggingEvent> listAppender = getLoggingEventListAppender();

    List<ILoggingEvent> logList = listAppender.list;

    profileRepository.saveAndFlush(profile);

    profile.setName("updated_name");

    profileRepository.saveAndFlush(profile);

    List<ILoggingEvent> filteredList =
        logList.stream().filter(o -> o.getMessage().equals(
            String.format(
                DATABASE_TRANSACTION_SUCCESSFUL, "update"))).toList();

    assertThat(filteredList).hasSize(1);

    assertThat(logList.get(3).getMessage()).isEqualTo(String.format(
        KAFKA_TRANSACTION_INITIALIZED, "update", PROFILE_ID));

    assertThat(logList.get(4).getMessage()).isEqualTo(String.format(
        DATABASE_TRANSACTION_SUCCESSFUL, "update"));

    assertThat(logList.get(5).getMessage()).isEqualTo(String.format(
        TRANSACTION_SUCCESSFUL, PROFILE_ID));
  }

  @Test
  void givenValidRequest_WhenSendingDelete_thenTransactionSyncLogsSuccessMessage()
      throws Exception {
    ListAppender<ILoggingEvent> listAppender = getLoggingEventListAppender();

    List<ILoggingEvent> logList = listAppender.list;

    profileRepository.save(profile);

    profileRepository.delete(profile);

    List<ILoggingEvent> filteredList =
        logList.stream().filter(o -> o.getMessage().equals(
            String.format(
                DATABASE_TRANSACTION_SUCCESSFUL, "delete"))).toList();

    assertThat(filteredList).hasSize(1);

    assertThat(logList.get(3).getMessage() ).isEqualTo(String.format(
        KAFKA_TRANSACTION_INITIALIZED, "delete", PROFILE_ID));

    assertThat(logList.get(4).getMessage()).isEqualTo(String.format(
        DATABASE_TRANSACTION_SUCCESSFUL, "delete"));

    assertThat(logList.get(5).getMessage()).isEqualTo(String.format(
        TRANSACTION_SUCCESSFUL, PROFILE_ID));
  }

  @AfterEach
  void clear() {
    TransactionSynchronizationManager.clear();
  }

  private static ListAppender<ILoggingEvent> getLoggingEventListAppender() {
    Logger kafkaLogger = (Logger) LoggerFactory.getLogger(KafkaDbTransactionSynchronizer.class);

    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();

    kafkaLogger.addAppender(listAppender);
    return listAppender;
  }
}
