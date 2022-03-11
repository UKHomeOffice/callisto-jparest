package uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities;

import lombok.Getter;
import lombok.Setter;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.JoinColumn;

import java.util.Set;

@Resource(path = "dummyEntityAs")
@Entity(name = "dummyEntityA")
public class DummyEntityA {


    @Id
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    @ManyToMany
    @JoinTable(
            name = "dummyEntityA_dummyEntityB",
            joinColumns = @JoinColumn(name = "dummyEntityA"),
            inverseJoinColumns = @JoinColumn(name = "dummyEntityB"))
    private Set<DummyEntityB> dummyEntityBSet;



}
