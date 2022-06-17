package uk.gov.homeoffice.digital.sas.jparesttest.steps;

import net.thucydides.core.annotations.Step;
import uk.gov.homeoffice.digital.sas.jparesttest.utils.JsonHelper;

import java.util.List;

public class RequestJson {

    @Step
    public String createJsonObject(final String fileName, final String entityIdValue) {
        return JsonHelper.createJsonObject(fileName, entityIdValue);
    }

    @Step
    public String createMultipleJpaRestApiJsonObject(final String fileName, List<String> entityIdValue) {
        return JsonHelper.createJpaRestApiJsonObjectWithManipulation(fileName, entityIdValue);
    }
}
