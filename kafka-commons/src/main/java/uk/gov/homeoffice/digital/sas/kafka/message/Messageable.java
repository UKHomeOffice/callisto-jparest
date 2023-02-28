package uk.gov.homeoffice.digital.sas.kafka.message;

import java.io.Serializable;

public interface Messageable extends Serializable {
  String resolveMessageKey();
}
