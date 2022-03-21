package uk.gov.homeoffice.digital.sas.jparest.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ConstantHelper {

    public static final String ID_PARAM_NAME =  "id";
    public static final String URL_ID_PATH_PARAM =  "/{" + ID_PARAM_NAME + "}";
    public static final String RELATED_PARAM_NAME =  "relatedId";
    public static final String URL_RELATED_ID_PATH_PARAM =  "/{" + RELATED_PARAM_NAME + "}";
    public static final String API_ROOT_PATH = "/resources";
    public static final String PATH_DELIMITER = "/";

}
