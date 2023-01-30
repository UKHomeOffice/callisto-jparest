package uk.gov.homeoffice.digital.sas.jparest.utils;

import java.util.List;

public class PayloadCreator {

    public static String createIDPayload(List<String> fieldNameList, List<String> fieldValueList){
        StringBuilder payload = new StringBuilder("{\n");
        for (int i = 0; i < fieldNameList.size() && i < fieldValueList.size(); i++){
            payload.append("\"").append(fieldNameList.get(i)).append("\": \"").append(fieldValueList.get(i)).append("\"");
            if(i != fieldNameList.size()-1 && i!= fieldNameList.size()-1){
                payload.append(",\n");
            }
        }
        payload.append("}");
        return payload.toString();
    }
}
