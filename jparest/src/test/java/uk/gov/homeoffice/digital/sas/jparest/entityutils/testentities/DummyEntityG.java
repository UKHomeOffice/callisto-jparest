package uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities;

import lombok.Getter;
import lombok.Setter;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import java.util.Set;

/**
 * A dummy entity that has relations to an entity that does not extend BaseEntity
 */

@Resource(path = "dummyEntityGs")
@Entity(name = "dummyEntityG")
public class DummyEntityG extends BaseEntity {


    @Getter
    @Setter
    @ManyToMany
    @JoinTable(name = "dummy_EntityA_dummy_EntityB",
            joinColumns = @JoinColumn(name = "dummy_EntityA"),
            inverseJoinColumns = @JoinColumn(name = "dummy_EntityB"))
    private Set<DummyEntityH> dummyEntityHSet;

    @Getter
    @Setter
    private Long profileId;

}
