package uk.gov.homeoffice.digital.sas.jparest.web;

public enum SupportedPatchOperations {

  REPLACE("replace");

  private final String stringValue;

  SupportedPatchOperations(final String s) {
    stringValue = s;
  }

  @Override
  public String toString() {
    return stringValue;
  }

}
