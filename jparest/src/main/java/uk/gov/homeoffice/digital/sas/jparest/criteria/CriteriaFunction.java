package uk.gov.homeoffice.digital.sas.jparest.criteria;

// Enum to control the allowed functions in the querystring criteria
public enum CriteriaFunction {
    Eq,
    Lt,
    Le,
    Gt,
    Ge,
    Like,
    NotLike,
    In,
    NotIn,
    Between
}
