package uk.gov.homeoffice.digital.sas.jparest.controller.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class RequestParameterTest {



    @Test
    void getEnumByParamName_requestParamNameMatchesEnumConstant_constantReturned() {

        assertThat(RequestParameter.getEnumByParamName(RequestParameter.TENANT_ID.getParamName()))
                .isEqualTo(RequestParameter.TENANT_ID);
    }

    @Test
    void getEnumByParamName_requestParamNameDoesNotMatchEnumConstant_illegalArgumentExceptionThrown() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                () -> RequestParameter.getEnumByParamName("unknown name"));
    }

}
