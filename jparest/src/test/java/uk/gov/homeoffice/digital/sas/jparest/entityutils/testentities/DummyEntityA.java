package uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities;

import lombok.Getter;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.Set;

@Resource
@Entity(name = "dummyEntityA")
public class DummyEntityA {


    @Id
    @Getter
    private Long id;

    @ManyToMany
    private Set<DummyEntityB> dummyEntityBSet;



}
