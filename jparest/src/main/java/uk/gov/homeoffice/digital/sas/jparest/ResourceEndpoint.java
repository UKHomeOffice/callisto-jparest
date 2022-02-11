package uk.gov.homeoffice.digital.sas.jparest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;


public class ResourceEndpoint {
    
    @Getter
    private Map<String, Class<?>> endpoints = new HashMap<String, Class<?>>();

    @Getter
    private List<Class<?>> resourceTypes = new ArrayList<Class<?>>();
}
