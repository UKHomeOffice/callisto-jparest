package uk.gov.homeoffice.digital.sas.cucumberjparest.scenarios.expectations;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Named.named;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.error.AssertJMultipleFailuresError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.path.json.JsonPath;

class AssertionsTest {

    private static Stream<Arguments> invalidExpectations() {
        return Stream.of(
                Arguments.of(named("fieldDoesNotMatchType",
                        JsonPath.from("{ \"description\": \"not a uuid\"}")),
                        new FieldExpectation("description", UUID.class, ""),
                        "Expected value to be of type 'class java.util.UUID'"),

                Arguments.of(named("nullValueIsExpectedToNotBeNull",
                        JsonPath.from("{ \"description\": null}")),
                        new FieldExpectation("description", String.class, "isNotNull()"),
                        "Expecting actual not to be null"),

                Arguments.of(named("expectationIsNotValid",
                        JsonPath.from("{ \"description\": null}")),
                        new FieldExpectation("description", String.class, "gibberish"),
                        "Invalid expectation"),

                Arguments.of(named("expectationCannotBeParsed",
                        JsonPath.from("{ \"description\": null}")),
                        new FieldExpectation("description", String.class, "gibb/$%erish"),
                        "Invalid expectation"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidExpectations")
    void objectMeetsExpectations_throw_when(
            JsonPath objectUnderTest, FieldExpectation fieldExpectation, String expectedMessage) {

        ConfigurableBeanFactory configurableBeanFactory = Mockito.mock(
                ConfigurableBeanFactory.class);
        Assertions assertions = new Assertions(configurableBeanFactory);
        List<FieldExpectation> expectations = Arrays.asList(fieldExpectation);
        ObjectMapper objectMapper = new ObjectMapper();
        SoftAssertions softly = new SoftAssertions();
        assertions.objectMeetsExpectations(objectUnderTest, expectations, objectMapper, softly);

        validateAssertionError(softly, expectedMessage);

    }

    private static void validateAssertionError(SoftAssertions softly, String message) {

        assertThatThrownBy(() -> {
            softly.assertAll();
        })
                .isExactlyInstanceOf(AssertJMultipleFailuresError.class)
                .extracting("failures")
                .asList()
                .hasSize(1)
                .element(0)
                .extracting("message")
                .asInstanceOf(InstanceOfAssertFactories.STRING)
                .contains(message);
    }

    @Test
    void objectMeetsExpectations_nullObjectIsNull_doesNotThrow() {

        ConfigurableBeanFactory configurableBeanFactory = Mockito.mock(ConfigurableBeanFactory.class);
        Assertions assertions = new Assertions(configurableBeanFactory);

        List<FieldExpectation> expectations = Arrays.asList(
                new FieldExpectation("name", String.class, "isNull()"));
        ObjectMapper objectMapper = new ObjectMapper();
        SoftAssertions softly = new SoftAssertions();
        JsonPath objectUnderTest = JsonPath.from("{ \"name\": null} ");
        assertions.objectMeetsExpectations(objectUnderTest, expectations, objectMapper, softly);
        assertThatNoException().isThrownBy(() -> {
            softly.assertAll();
        });

    }
}
