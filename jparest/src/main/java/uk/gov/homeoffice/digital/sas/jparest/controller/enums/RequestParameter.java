package uk.gov.homeoffice.digital.sas.jparest.controller.enums;

import org.springframework.data.domain.Pageable;
import org.springframework.expression.spel.standard.SpelExpression;

import java.util.List;
import java.util.UUID;

public enum RequestParameter {

    TENANT_ID("tenantId", ParameterType.QUERY, UUID.class, true, 10),
    ID("id", ParameterType.PATH, UUID.class, true, 20),
    RELATION("relation", ParameterType.PATH, String.class, true, 30),
    RELATED_IDS("relatedIds", ParameterType.PATH, List.class, true, 40),
    PAGEABLE("pageable", ParameterType.QUERY, Pageable.class, true, 50),
    FILTER("filter", ParameterType.QUERY, SpelExpression.class, false, 60),
    BODY("body", ParameterType.BODY, String.class, true, 200);

    private final String paramName;
    private final ParameterType paramType;
    private final Class<?> paramDataType;
    private final boolean isRequired;
    private final int order;


    RequestParameter(String paramName, ParameterType paramType, Class<?> paramDataType, boolean isRequired, int order) {
        this.paramName = paramName;
        this.paramType = paramType;
        this.paramDataType = paramDataType;
        this.isRequired = isRequired;
        this.order = order;
    }


    public String getParamName() {
        return paramName;
    }

    public String getParamType() {
        return paramType.getType();
    }

    public Class<?> getParamDataType() {
        return paramDataType;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public int getOrder() {
        return order;
    }


    public static RequestParameter getEnumByParamName(String requestParamName) {
        for (RequestParameter requestParameter : values())
            if(requestParameter.getParamName().equals(requestParamName)) return requestParameter;

        throw new IllegalArgumentException(String.format(
                "No %s enum constant found for parameter name: %s ",  RequestParameter.class.getCanonicalName(), requestParamName));
    }


    enum ParameterType {

        QUERY("query"),
        PATH("path"),
        BODY("body");

        private final String type;

        ParameterType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

}
