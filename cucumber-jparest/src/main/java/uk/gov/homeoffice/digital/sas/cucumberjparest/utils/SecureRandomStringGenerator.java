package uk.gov.homeoffice.digital.sas.cucumberjparest.utils;

import java.security.SecureRandom;

public class SecureRandomStringGenerator {

  private SecureRandomStringGenerator() {}

  static final String ALPHABET_SET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final SecureRandom secureRandom = new SecureRandom();

  public static String randomAlphabetic(int count) {
    return randomAlphabetic(count, ALPHABET_SET);
  }

  public static String randomAlphabetic(int count, String characterBase) {
    return secureRandom.ints(count, 0, characterBase.length())
        .mapToObj(ALPHABET_SET::charAt)
        .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
        .toString();
  }

}
