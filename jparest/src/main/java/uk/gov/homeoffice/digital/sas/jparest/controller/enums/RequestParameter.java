package uk.gov.homeoffice.digital.sas.jparest.controller.enums;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum RequestParameter {

    TENANT_ID("tenantId", ParameterType.QUERY, true, 10),
    ID("id", ParameterType.PATH, true, 20),
    RELATED_IDS("relatedIds", ParameterType.PATH, true, 30),
    PAGEABLE("pageable", ParameterType.QUERY, true, 40),
    FILTER("filter", ParameterType.QUERY, false, 50);

    private final String paramName;
    private final ParameterType paramType;
    private final boolean isRequired;
    private final int order;


    RequestParameter(String paramName, ParameterType paramType, boolean isRequired, int order) {
        this.paramName = paramName;
        this.paramType = paramType;
        this.isRequired = isRequired;
        this.order = order;
    }


    public String getParamName() {
        return paramName;
    }

    public String getParamType() {
        return paramType.getType();
    }

    public boolean isRequired() {
        return isRequired;
    }

    public int getOrder() {
        return order;
    }


    public static RequestParameter getEnumByParamName(String queryParamName) {
        for (RequestParameter requestParameter : values())
            if(requestParameter.getParamName().equals(queryParamName)) return requestParameter;

        throw new IllegalArgumentException(String.format(
                "No %s enum constant found for parameter name: %s ",  RequestParameter.class.getCanonicalName(), queryParamName));
    }

    public static List<RequestParameter> getSortedParams(RequestParameter... requestParameters) {
        return Stream.of(requestParameters)
                .sorted(Comparator.comparing(RequestParameter::getOrder))
                .collect(Collectors.toList());
    }



    enum ParameterType {

        QUERY("query"),
        PATH("path");

        private final String type;

        ParameterType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

}
