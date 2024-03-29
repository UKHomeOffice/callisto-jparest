package uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities;

import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;

@Resource(path = "dummyEntityAs")
@Entity(name = "dummyEntityA")
@EqualsAndHashCode
public class DummyEntityA extends BaseEntity {

    @Getter
    @Setter
    @ManyToMany
    @JoinTable(name = "dummy_EntityA_dummy_EntityB", joinColumns = @JoinColumn(name = "dummy_EntityA"), inverseJoinColumns = @JoinColumn(name = "dummy_EntityB"))
    private Set<DummyEntityB> dummyEntityBSet;

    @Getter
    @Setter
    private Long profileId;

    @Getter
    @Setter
    private Long index;

}
