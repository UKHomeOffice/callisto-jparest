package uk.gov.homeoffice.digital.sas.cucumberjparest.utils;

import java.security.SecureRandom;

public class SecureRandomStringGenerator {

  private SecureRandomStringGenerator() {}

  static final String ALPHABET_SET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final SecureRandom secureRandom = new SecureRandom();

  public static String randomAlphabetic(int size) {
    return random(size, ALPHABET_SET);
  }

  /**
   * Securely generate a random set of characters of size {@code size}, using {@code characterBase}
   * as a base.

   * @param size size of generated string
   * @param characterBase base of characters used to generate the random string
   * @return random string of the specified size, using the specified character base
   */
  public static String random(int size, String characterBase) {
    return secureRandom.ints(size, 0, characterBase.length())
        .mapToObj(ALPHABET_SET::charAt)
        .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
        .toString();
  }

}
