package uk.gov.homeoffice.digital.sas.jparest.utils;

import java.util.List;

public class PayloadCreator {

    public static String createIDPayload(List<String> fieldName, List<String> fieldValue){
        StringBuilder payload = new StringBuilder("{\n");
        for (int i = 0; i < fieldName.size(); i++){
            payload.append("\"").append(fieldName.get(i)).append("\": \"").append(fieldValue.get(i)).append("\"");
        }
        payload.append("}");
        return payload.toString();
    }
}
