package uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities;

import jakarta.persistence.Entity;

import java.time.LocalDate;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;

@Resource
@Entity(name = "dummyEntityC")
public class DummyEntityC extends BaseEntity {

    @Getter
    @Setter
    private String description;

    @Getter
    @Setter
    private Long index;

    @Getter
    @Setter
    private Long profileId;

    @Getter
    @Setter
    private LocalDate dob;

    @Getter
    @Setter
    private Instant instant;
}
