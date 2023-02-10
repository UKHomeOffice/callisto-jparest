package uk.gov.homeoffice.digital.sas.jparest.editor;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CustomInstantEditorTest {

  private final CustomInstantEditor instantEditor = new CustomInstantEditor();

  static Stream<Arguments> testData() {

    return Stream.of(
        Arguments.of((Object) new Object[] {
            "1901-05-21T00:00:00.000+00:00",
            Instant.parse("1901-05-21T00:00:00.000+00:00"),
            "1901-05-21T00:00:00Z"
        }),
        Arguments.of((Object) new Object[] {" ", null, ""}),
        Arguments.of((Object) new Object[] {null, null, ""})
    );
  }

  @ParameterizedTest
  @MethodSource("testData")
  void instantEditor(Object[] args) {
    String inputText = (String) args[0];
    Object expectedParsedProperty = args[1];
    String expectedPropertyAsText = (String) args[2];

    instantEditor.setAsText(inputText);
    assertThat(instantEditor.getValue()).isEqualTo(expectedParsedProperty);
    assertThat(instantEditor.getAsText()).isEqualTo(expectedPropertyAsText);
  }
}