package uk.gov.homeoffice.digital.sas.jparest.utils;

import lombok.experimental.UtilityClass;
import uk.gov.homeoffice.digital.sas.jparest.controller.enums.RequestParameter;

@UtilityClass
@SuppressWarnings("squid:S1075")
public class ConstantHelper {

    public static final String URL_ID_PATH_PARAM =  "/{" + RequestParameter.ID.getParamName() + "}";
    public static final String URL_RELATED_ID_PATH_PARAM =  "/{" + RequestParameter.RELATED_IDS.getParamName() + "}";
    public static final String API_ROOT_PATH = "/resources";
    public static final String PATH_DELIMITER = "/";

}
