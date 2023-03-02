package uk.gov.homeoffice.digital.sas.kafka.transactionsync;

import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.DATABASE_TRANSACTION_FAILED;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.DATABASE_TRANSACTION_SUCCESSFUL;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_TRANSACTION_INITIALIZED;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.TRANSACTION_SUCCESSFUL;

import java.util.function.Consumer;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaAction;
import uk.gov.homeoffice.digital.sas.kafka.message.Messageable;

@Component
public class KafkaDbTransactionSynchronizer<T extends Messageable> {

  private static final Logger log = LoggerFactory.getLogger(KafkaDbTransactionSynchronizer.class);

  public void registerSynchronization(KafkaAction action, T resource,
                                      Consumer<String> sendKafkaMessage) {

    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          int status = TransactionSynchronization.STATUS_UNKNOWN;
          String messageKey;

          @SneakyThrows
          @Override
          public void beforeCommit(boolean readOnly) {
            messageKey = resource.resolveMessageKey();
            log.info(String.format(KAFKA_TRANSACTION_INITIALIZED,
                action, messageKey));
            sendKafkaMessage.accept(messageKey);
          }

          @Override
          public void afterCommit() {
            log.info(String.format(DATABASE_TRANSACTION_SUCCESSFUL,
                action));
            status = TransactionSynchronization.STATUS_COMMITTED;
          }

          @Override
          public void afterCompletion(int status) {
            if (status == STATUS_COMMITTED) {
              log.info(String.format(
                  TRANSACTION_SUCCESSFUL, messageKey));

            } else {
              log.error(String.format(
                  DATABASE_TRANSACTION_FAILED,
                  action));
            }
          }
        }
    );
  }

}