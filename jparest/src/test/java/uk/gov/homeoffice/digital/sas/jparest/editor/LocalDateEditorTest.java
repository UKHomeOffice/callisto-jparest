package uk.gov.homeoffice.digital.sas.jparest.editor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class LocalDateEditorTest {

  private final LocalDateEditor customEditor = new LocalDateEditor();

  static Stream<Arguments> testData() {

    return Stream.of(
        Arguments.of((Object) new Object[] {
            "1901-05-21",
            LocalDate.parse("1901-05-21"),
            "1901-05-21"
        }),
        Arguments.of((Object) new Object[] {" ", null, ""}),
        Arguments.of((Object) new Object[] {null, null, ""})
    );
  }

  @ParameterizedTest
  @MethodSource("testData")
  void customEditor(Object[] args) {
    String inputText = (String) args[0];
    Object expectedParsedProperty = args[1];
    String expectedPropertyAsText = (String) args[2];

    customEditor.setAsText(inputText);
    assertThat(customEditor.getValue()).isEqualTo(expectedParsedProperty);
    assertThat(customEditor.getAsText()).isEqualTo(expectedPropertyAsText);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "1901-05-21T00:00:00.000+00:00",
      "1901-05-21T00:00:00.000",
      "1901-05-21T00:00",
      "01-05-21",
      "1901-05"
  })
  void customEditor_invalidInput(String invalidInput) {
    assertThatExceptionOfType(DateTimeParseException.class)
        .isThrownBy(() -> customEditor.setAsText(invalidInput));
  }
}