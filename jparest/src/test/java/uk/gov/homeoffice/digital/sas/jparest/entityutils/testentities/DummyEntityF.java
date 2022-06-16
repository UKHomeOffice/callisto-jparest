package uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Resource(path = "dummy_EntityFs")
@Entity(name = "dummy_EntityF")
@NoArgsConstructor
@Getter @Setter
public class DummyEntityF extends BaseEntity {

    @OneToOne(optional=false)
    @JoinColumn(name="dummy_entityC_id", unique=true, nullable=false, updatable=false)
    private DummyEntityC dummyEntityC;

}
