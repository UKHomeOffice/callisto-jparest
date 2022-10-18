package uk.gov.homeoffice.digital.sas.jparest.controller.enums;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import org.springframework.data.domain.Pageable;
import org.springframework.expression.spel.standard.SpelExpression;

@Getter
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


  RequestParameter(String paramName, ParameterType paramType,
                   Class<?> paramDataType, boolean isRequired, int order) {
    this.paramName = paramName;
    this.paramType = paramType;
    this.paramDataType = paramDataType;
    this.isRequired = isRequired;
    this.order = order;
  }

  public String getParamType() {
    return paramType.getType();
  }


  public static Map<String, RequestParameter> getParamNameToEnumMap() {
    return Stream.of(values())
      .collect(Collectors.toMap(RequestParameter::getParamName, Function.identity()));
  }


  enum ParameterType {

    QUERY("query"),
    PATH("path"),
    BODY("body");

    @Getter
    private final String type;

    ParameterType(String type) {
      this.type = type;
    }

  }

}
