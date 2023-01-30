package uk.gov.homeoffice.digital.sas.jparest.testutils.payload;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.Map;

public final class PayloadCreator {

    private PayloadCreator(){
        // no instantiation
    }

    public static <S extends Serializable> String createPayload(Map<String, S> fieldHashMap){
        return new Gson().toJson(fieldHashMap);
    }
}
