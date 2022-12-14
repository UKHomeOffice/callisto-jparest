package uk.gov.homeoffice.digital.sas.cucumberjparest.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SerialisationUtil {

  private SerialisationUtil() {}

  public static Map<String, String> stringToMap(String serialisedMap) {
    return stringToMap(serialisedMap, ",", "=");
  }

  public static Map<String, String> stringToMap(String serialisedMap, String entrySeparator,
      String keyValueSeparator) {
    if (serialisedMap == null || serialisedMap.isBlank()) {
      return new HashMap<>();
    }
    return Arrays.stream(serialisedMap.split(entrySeparator))
        .map(s -> s.split(keyValueSeparator))
        .collect(Collectors.toMap(s -> s[0], s -> s[1]));
  }
}
