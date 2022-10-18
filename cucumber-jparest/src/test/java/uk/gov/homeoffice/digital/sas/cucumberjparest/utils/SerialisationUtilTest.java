package uk.gov.homeoffice.digital.sas.cucumberjparest.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SerialisationUtilTest {

    @ParameterizedTest
    @MethodSource("stringToMapTestData")
    public void GIVEN_serialised_map_WHEN_converted_to_map_THEN_produces_the_expected_map(String serialisedMap, int expectedMapSize, Map<String, String> expectedMap) {
        Map<String, String> actualMap = SerialisationUtil.stringToMap(serialisedMap);
        assertEquals(expectedMapSize, actualMap.size());
        expectedMap.forEach((key, value) -> assertEquals(value, actualMap.get(key)));
    }

    private static Stream<Arguments> stringToMapTestData() {
        return Stream.of(
                Arguments.of("key1=value1,key2=value2", 2, Map.of("key1", "value1", "key2", "value2")),
                Arguments.of("key1=value1,key2=value2,key3=value3", 3, Map.of("key1", "value1", "key2", "value2", "key3", "value3")),
                Arguments.of(null, 0, Map.of()),
                Arguments.of("", 0, Map.of())
        );
    }
}