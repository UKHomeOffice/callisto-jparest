package uk.gov.homeoffice.digital.sas.cucumberjparest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Expectation {

    @NonNull
    private String field;
    @NonNull
    private Class<?> type;
    @NonNull
    private String expectation;
}
