package uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import lombok.Getter;
import lombok.Setter;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;

@Resource(path = "dummyEntityBs")
@Entity(name = "dummyEntityB")
public class DummyEntityB extends BaseEntity {

    @ManyToMany(mappedBy = "dummyEntityBSet")
    private Set<DummyEntityA> dummyEntityASet;

}
