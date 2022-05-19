package uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;

@Resource(path = "dummyEntityAs")
@Entity(name = "dummyEntityA")
@Data
public class DummyEntityA extends BaseEntity {

    @ManyToMany
    @JoinTable(name = "dummy_EntityA_dummy_EntityB", joinColumns = @JoinColumn(name = "dummy_EntityA"), inverseJoinColumns = @JoinColumn(name = "dummy_EntityB"))
    private Set<DummyEntityB> dummyEntityBSet;

    private Long profileId;

}
