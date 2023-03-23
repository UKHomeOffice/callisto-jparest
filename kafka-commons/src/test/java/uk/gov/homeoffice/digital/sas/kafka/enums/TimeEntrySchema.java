package uk.gov.homeoffice.digital.sas.kafka.enums;

public enum TimeEntrySchema {

  V0_0_1("0.0.1"),
  V0_0_2("0.0.2");
  private String version;

  TimeEntrySchema(String versionString) {
    this.version = versionString;
  }

  public String getVersion() {
    return version;
  }
}
