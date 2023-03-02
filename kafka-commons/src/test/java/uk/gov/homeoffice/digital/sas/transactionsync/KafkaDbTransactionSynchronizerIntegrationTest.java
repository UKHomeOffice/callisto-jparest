package uk.gov.homeoffice.digital.sas.transactionsync;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.DATABASE_TRANSACTION_SUCCESSFUL;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_TRANSACTION_INITIALIZED;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.TRANSACTION_SUCCESSFUL;
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
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaAction;
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
class KafkaDbTransactionSynchronizerIntegrationTest {
  private static final String PROFILE_NAME = "Original profile";
  private Profile profile;

  private String messageKey;

  @Autowired
  private ProfileRepository profileRepository;

  @BeforeEach
  void setup() {
    TransactionSynchronizationManager.initSynchronization();
    Long tenantId = new Random().nextLong();
    profile = new Profile(null, String.valueOf(tenantId), PROFILE_NAME);
  }

  @Test
  void givenValidRequest_whenSendingCreateRequest_thenTransactionSyncLogsSuccessMessage() {
    ListAppender<ILoggingEvent> listAppender = getLoggingEventListAppender();

    List<ILoggingEvent> logList = listAppender.list;

    profileRepository.saveAndFlush(profile);

    messageKey = profile.resolveMessageKey();

    assertThat(logList.get(0).getMessage()).isEqualTo(String.format(
        KAFKA_TRANSACTION_INITIALIZED,
        KafkaAction.CREATE , messageKey));
    assertThat(logList.get(1).getMessage()).isEqualTo(String.format(
        DATABASE_TRANSACTION_SUCCESSFUL, KafkaAction.CREATE));
    assertThat(logList.get(2).getMessage()).isEqualTo(String.format(
        TRANSACTION_SUCCESSFUL, messageKey));

  }

  @Test
  void givenValidRequest_whenSendingUpdateRequest_thenTransactionSyncLogsSuccessMessage() {
    ListAppender<ILoggingEvent> listAppender = getLoggingEventListAppender();

    List<ILoggingEvent> logList = listAppender.list;

    profileRepository.saveAndFlush(profile);
    profile.setName("updated_name");
    profileRepository.saveAndFlush(profile);

    messageKey = profile.resolveMessageKey();

    List<ILoggingEvent> filteredList =
        logList.stream().filter(o -> o.getMessage().equals(
            String.format(
                DATABASE_TRANSACTION_SUCCESSFUL, KafkaAction.UPDATE))).toList();

    assertThat(filteredList).hasSize(1);

    assertThat(logList.get(3).getMessage()).isEqualTo(String.format(
        KAFKA_TRANSACTION_INITIALIZED, KafkaAction.UPDATE, messageKey));

    assertThat(logList.get(4).getMessage()).isEqualTo(String.format(
        DATABASE_TRANSACTION_SUCCESSFUL, KafkaAction.UPDATE));

    assertThat(logList.get(5).getMessage()).isEqualTo(String.format(
        TRANSACTION_SUCCESSFUL, messageKey));
  }

  @Test
  void givenValidRequest_whenSendingDelete_thenTransactionSyncLogsSuccessMessage() {
    ListAppender<ILoggingEvent> listAppender = getLoggingEventListAppender();

    List<ILoggingEvent> logList = listAppender.list;

    profileRepository.save(profile);

    profileRepository.delete(profile);

    messageKey = profile.resolveMessageKey();

    List<ILoggingEvent> filteredList =
        logList.stream().filter(o -> o.getMessage().equals(
            String.format(
                DATABASE_TRANSACTION_SUCCESSFUL, KafkaAction.DELETE))).toList();

    assertThat(filteredList).hasSize(1);

    assertThat(logList.get(3).getMessage() ).isEqualTo(String.format(
        KAFKA_TRANSACTION_INITIALIZED, KafkaAction.DELETE, messageKey));

    assertThat(logList.get(4).getMessage()).isEqualTo(String.format(
        DATABASE_TRANSACTION_SUCCESSFUL, KafkaAction.DELETE));

    assertThat(logList.get(5).getMessage()).isEqualTo(String.format(
        TRANSACTION_SUCCESSFUL, messageKey));
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
