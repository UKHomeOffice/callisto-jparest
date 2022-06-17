package uk.gov.homeoffice.digital.sas.jparesttest.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@UtilityClass
@Slf4j
public class JsonHelper {

    public static final String ENTITY_ID_VALUE = "9999999999";

    public static String createJsonObject(final String fileName, final String entityIdValue) {
        try {
            String payload = new String(Files.readAllBytes(Paths.get(fileName)));
            return payload.contains(ENTITY_ID_VALUE) ? payload.replace(ENTITY_ID_VALUE, entityIdValue) : payload;
        } catch (IOException e) {
            log.error("Error While reading file: {} - {}", fileName, e.getMessage());
        }
        return EMPTY;
    }
}