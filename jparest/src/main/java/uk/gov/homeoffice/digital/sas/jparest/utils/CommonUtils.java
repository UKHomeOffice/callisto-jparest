package uk.gov.homeoffice.digital.sas.jparest.utils;

import java.util.Arrays;

public final class CommonUtils {

  private CommonUtils() {
    throw new AssertionError();
  }

  public static String getFieldNameOrThrow(Class<?> clazz, String fieldName) {
    return Arrays.stream(clazz.getDeclaredFields())
        .filter(field -> field.getName().equals(fieldName)).findFirst()
        .orElseThrow(() -> new RuntimeException(
                String.format("Unable to find [%s] field within %s class",
                        fieldName, clazz.getSimpleName())))
            .getName();
  }

}
