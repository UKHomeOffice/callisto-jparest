package uk.gov.homeoffice.digital.sas.cucumberjparest.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class SerialisationUtilTest {

    @ParameterizedTest
    @MethodSource("stringToMapTestData")
    void stringToMap_serialisedMapStringProvided_produceExpectedMapObject(String serialisedMap, int expectedMapSize, Map<String, String> expectedMap) {
        Map<String, String> actualMap = SerialisationUtil.stringToMap(serialisedMap);
        assertThat(actualMap).hasSize(expectedMapSize);
        expectedMap.forEach((key, value) -> assertThat(actualMap).containsEntry(key,value));
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