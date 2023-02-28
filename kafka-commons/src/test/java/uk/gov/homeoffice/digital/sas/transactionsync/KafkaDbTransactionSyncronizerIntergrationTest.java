package uk.gov.homeoffice.digital.sas.transactionsync;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.DATABASE_TRANSACTION_SUCCESSFUL;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_TRANSACTION_INITIALIZED;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.TRANSACTION_SUCCESSFUL;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import uk.gov.homeoffice.digital.sas.ProfileApplication;
import uk.gov.homeoffice.digital.sas.kafka.transactionsync.KafkaDbTransactionSynchronizer;
import uk.gov.homeoffice.digital.sas.model.Profile;

@DirtiesContext
@WebAppConfiguration
@AutoConfigureMockMvc(addFilters = true)
@EmbeddedKafka(topics = "callisto-timecard",
    bootstrapServersProperty = "spring.kafka.bootstrap-servers")
@SpringBootTest(classes = ProfileApplication.class)
public class KafkaDbTransactionSyncronizerIntergrationTest {
  private static final Long PROFILE_ID = new Random().nextLong();
  private static final String PROFILE_NAME = "Original profile";
  private Profile profile;

  private static ObjectMapper mapper = new ObjectMapper();

  @Autowired
  private MockMvc mockMvc;

  @BeforeEach
  void setup() {
    profile = new Profile(String.valueOf(PROFILE_ID), PROFILE_NAME);
  }

  @Test
  void givenValidRequest_WhenSendingCreateRequest_thenTransactionSyncLogsSuccessMessage()
      throws Exception {
    ListAppender<ILoggingEvent> listAppender = getLoggingEventListAppender();
    //
    List<ILoggingEvent> logList = listAppender.list;

    persistProfile(profile);
    TransactionSynchronizationManager.initSynchronization();

    assertThat(String.format(
        KAFKA_TRANSACTION_INITIALIZED,
        "create" , PROFILE_ID)).isEqualTo(logList.get(0).getMessage());

    assertThat(logList.get(1).getMessage()).isEqualTo(String.format(
        DATABASE_TRANSACTION_SUCCESSFUL, "create", ""));
    assertThat(logList.get(2).getMessage()).isEqualTo(String.format(
        TRANSACTION_SUCCESSFUL, PROFILE_ID));

  }

  @Test
  void givenValidRequest_WhenSendingUpdateRequest_thenTransactionSyncLogsSuccessMessage()
      throws Exception {
    ListAppender<ILoggingEvent> listAppender = getLoggingEventListAppender();

    List<ILoggingEvent> logList = listAppender.list;

    persistProfile(profile);
    TransactionSynchronizationManager.initSynchronization();

    String updatedName = "updated_name";

    Profile profileUpdate = new Profile(String.valueOf(PROFILE_ID), updatedName);
    TransactionSynchronizationManager.clear();
    mockMvc.perform(put("/profiles/" + PROFILE_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectAsJsonString(profileUpdate)))
        .andDo(print())
        .andExpect(status().isOk());
    TransactionSynchronizationManager.initSynchronization();
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

  @AfterEach
  void clear() {
    TransactionSynchronizationManager.clear();
  }

  private void persistProfile(Profile profile) throws Exception {
    MvcResult mvcResult = mockMvc.perform(post("/profiles/" + PROFILE_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectAsJsonString(profile)))
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();
  }

  private static ListAppender<ILoggingEvent> getLoggingEventListAppender() {
    Logger kafkaLogger = (Logger) LoggerFactory.getLogger(KafkaDbTransactionSynchronizer.class);

    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();

    kafkaLogger.addAppender(listAppender);
    return listAppender;
  }

  public static  String objectAsJsonString(final Object obj) throws JsonProcessingException {
    return mapper.writeValueAsString(obj);
  }

}
