package uk.gov.homeoffice.digital.sas.cucumberjparest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.homeoffice.digital.sas.cucumberjparest.api.PayloadManager;
import uk.gov.homeoffice.digital.sas.cucumberjparest.api.PayloadManager.PayloadKey;

class PayloadManagerTest {

  private final PayloadManager payloadManager = new PayloadManager();
  private final PayloadKey payloadKey1 = new PayloadKey("", "");
  private final PayloadKey payloadKey2 = new PayloadKey("", "");

  @BeforeEach
  void setup(){
    payloadKey1.setResourceType("profiles");
    payloadKey1.setName("valid");
    payloadKey2.setResourceType("profiles");
    payloadKey2.setName("invalid");
    payloadManager.createPayload(payloadKey1, "Some content");
  }

  @Test
  void shouldReturnPayloadWhenExists() {
    assertThat(payloadManager.getPayload(payloadKey1)).isEqualTo("Some content");
  }

  @Test
  void shouldComparePayloadKeysAsExpected() {
    assertThat(payloadKey1.getResourceType()).isEqualTo(payloadKey2.getResourceType());
    assertThat(payloadKey1.getName()).isNotEqualTo(payloadKey2.getName());
    assertThat(payloadKey1).isNotEqualTo(payloadKey2).isNotNull();
  }

  @Test
  void shouldThrowAnExceptionWhenPayloadNotFound() {
    IllegalArgumentException thrown = assertThrows(
        IllegalArgumentException.class,
        () -> payloadManager.getPayload(payloadKey2),
        "Expected createPayload() to throw an exception, but it didn't"
    );

    assertThat(thrown.getMessage())
        .isEqualTo(
            "A payload with the name \"" + payloadKey2.getName() + "\" does not exist"
        );
  }

  @Test
  void shouldThrowAnExceptionWhenCreatingPayloadWhichAlreadyExists() {
    IllegalArgumentException thrown = assertThrows(
        IllegalArgumentException.class,
        () -> payloadManager.createPayload(payloadKey1, ""),
        "Expected createPayload() to throw an exception, but it didn't"
    );

    assertThat(thrown.getMessage())
        .isEqualTo(
            "A payload with the name \"" + payloadKey1.getName() + "\" already exists"
        );
  }
}