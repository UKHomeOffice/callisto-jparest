package uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import lombok.Getter;
import lombok.Setter;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;

@Resource(path = "dummyEntityAs")
@Entity(name = "dummyEntityA")
public class DummyEntityA extends BaseEntity {

    @Getter
    @Setter
    @ManyToMany
    @JoinTable(name = "dummy_EntityA_dummy_EntityB", joinColumns = @JoinColumn(name = "dummy_EntityA"), inverseJoinColumns = @JoinColumn(name = "dummy_EntityB"))
    private Set<DummyEntityB> dummyEntityBSet;

    @Getter
    @Setter
    private Long profileId;

}
