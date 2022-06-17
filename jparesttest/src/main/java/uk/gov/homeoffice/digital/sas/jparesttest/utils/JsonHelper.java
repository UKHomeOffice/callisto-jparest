package uk.gov.homeoffice.digital.sas.jparesttest.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@UtilityClass
@Slf4j
public class JsonHelper {

    public static final String ENTITY_ID_VALUE = "9999999999";
    public static String VALUE_REPLACEMENT = "replace0";

    public static String createJsonObject(final String fileName, final String entityIdValue) {
        try {
            String payload = new String(Files.readAllBytes(Paths.get(fileName)));
            return payload.contains(ENTITY_ID_VALUE) ? payload.replace(ENTITY_ID_VALUE, entityIdValue) : payload;
        } catch (IOException e) {
            log.error("Error While reading file: {} - {}", fileName, e.getMessage());
        }
        return EMPTY;
    }

    public static String createJpaRestApiJsonObjectWithManipulation(final String fileName, final List<String> entityIdValue) {
        try {
            String payload = new String(Files.readAllBytes(Paths.get(fileName)));
            for (int i = 0; i <= entityIdValue.size(); ) {
                if (payload.contains(VALUE_REPLACEMENT)) {
                    payload = payload.replace(VALUE_REPLACEMENT, entityIdValue.get(i));
                }
                int replaceIndex = i + 1;
                VALUE_REPLACEMENT = VALUE_REPLACEMENT.replace("replace" + i, "replace" + replaceIndex);
                i++;
            }
            VALUE_REPLACEMENT = "replace0";
            return payload;
        } catch (IOException e) {
            log.error("Error While reading file: {} - {}", fileName, e.getMessage());
        }
        return EMPTY;
    }
}