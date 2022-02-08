package uk.gov.homeoffice.digital.sas.jparest.criteria;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class Criteria {
    
    private String fieldName;
    private CriteriaFunction function;
    private String value;

}
