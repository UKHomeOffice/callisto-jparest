package uk.gov.homeoffice.digital.sas.cucumberjparest;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
public class PayloadManager {

  // Holds that state for payloads
  private final Map<PayloadKey, String> payloads = new HashMap<>();

  /**
   * Creates a new payload with the given name.
   *
   * @param key     The key to use for the payload consisting of resource type and name
   * @param content The content of the payload
   */
  public void createPayload(PayloadKey key, String content) {
    if (payloads.containsKey(key)) {
      throw new IllegalArgumentException(
          "A payload with the name \"" + key.name + "\" already exists");
    }

    payloads.put(key, content);
  }

  /**
   * Retrieves a payload added by
   *     the {@link PayloadManager#createPayload(PayloadKey, String)} method.
   *
   * @param key The name used to reference the payload
   * @return Payload
   */
  public String getPayload(PayloadKey key) {

    if (!payloads.containsKey(key)) {
      throw new IllegalArgumentException(
          "A payload with the name \"" + key.name + "\" does not exist");
    }
    return payloads.get(key);
  }

  /**
   * Used to hold a key for a defined payload. These take the form of the resource type and a
   * variable that can be used to reference the payload throughout a scenario
   */
  @AllArgsConstructor
  public static class PayloadKey {

    @NonNull
    @Getter
    @Setter
    private String resourceType;

    @NonNull
    @Getter
    @Setter
    private String name;

    @Override
    public final int hashCode() {
      return (resourceType + name).hashCode();
    }

    @Override
    public final boolean equals(Object o) {
      if (o == null) {
        return false;
      }
      if (o.getClass() != PayloadKey.class) {
        return false;
      }
      PayloadKey payloadKey = (PayloadKey) o;
      return this.name.equals(payloadKey.name) && this.resourceType.equals(payloadKey.resourceType);
    }
  }

}
