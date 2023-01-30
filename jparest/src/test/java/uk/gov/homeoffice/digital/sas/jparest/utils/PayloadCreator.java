package uk.gov.homeoffice.digital.sas.jparest.utils;

import com.google.gson.Gson;

import java.util.Map;

public class PayloadCreator {

    public static String createIDPayload(Map<String, String> fieldHashMap){
        return new Gson().toJson(fieldHashMap);
    }
}
