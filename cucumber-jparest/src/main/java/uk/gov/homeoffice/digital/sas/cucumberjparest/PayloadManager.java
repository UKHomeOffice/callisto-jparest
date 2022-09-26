package uk.gov.homeoffice.digital.sas.cucumberjparest;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class PayloadManager {

    // Holds that state for payloads
    private Map<String, Payload> payloads = new HashMap<>();

    /**
     * 
     * Creates a new payload with the given name
     * 
     * @param name         The name used to reference the payload
     * @param resourceType The type of resource represented by the payload
     * @param content      The content of the payload
     * @return Payload
     */
    public Payload createPayload(String name, String resourceType, String content) {
        String payloadKey = getPayloadKey(name, resourceType);
        if (payloads.containsKey(payloadKey)) {
            throw new IllegalArgumentException("A payload with the name \"" + payloadKey + "\" already exists");
        }

        Payload payload = new Payload(resourceType, content);
        payloads.put(payloadKey, payload);
        return payload;
    }

    /**
     * 
     * Retrieves a payload added by the
     * {@link #createPayload(String, String, String)} method
     * 
     * @param name The name used to reference the payload
     * @return Payload
     */
    public Payload getPayload(String name, String resourceType) {

        String payloadKey = getPayloadKey(name, resourceType);

        if (!payloads.containsKey(payloadKey)) {
            throw new IllegalArgumentException("A payload with the name \"" + payloadKey + "\" does not exist");
        }
        return payloads.get(payloadKey);
    }

    /**
     * 
     * Returns a key for the payload
     * 
     * @param name         The name given to the payload
     * @param resourceType The resource type represented
     * @return String
     */
    private String getPayloadKey(String name, String resourceType) {
        return name + " " + resourceType;
    }

}
