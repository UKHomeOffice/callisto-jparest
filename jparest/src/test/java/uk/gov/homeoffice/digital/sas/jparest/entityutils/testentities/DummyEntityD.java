package uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities;

import io.swagger.v3.oas.annotations.media.ExampleObject;
import lombok.Getter;
import lombok.Setter;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;


//A dummy test entity with a blank ExampleObject for error path testing

@Resource(filterExamples = {@ExampleObject})
@Entity(name = "dummyEntityD")

public class DummyEntityD extends BaseEntity {

    @Getter
    @Setter
    @NotEmpty
    private String description;

    @Getter
    @Setter
    @NotEmpty
    @Positive
    @Digits(fraction = 0, integer = 5)
    private String telephone;

}
