package uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;

@Resource
@Entity(name = "dummyEntityC" )
@Data
public class DummyEntityC extends BaseEntity {

    private String description;

    private Long index;

    private Long profileId;

}
