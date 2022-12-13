package uk.gov.homeoffice.digital.sas.jparest.models;

import java.io.Serializable;

public interface Message extends Serializable {

  String getMessageKey();
}