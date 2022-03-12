package uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;

@Resource
@Entity(name = "dummyEntityC")
public class DummyEntityC extends BaseEntity {

    @Id
    @Getter
    private Long id;

    @Getter
    @Setter
    private String description;

}
