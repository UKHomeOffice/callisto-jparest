package uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities;

import lombok.Getter;
import lombok.Setter;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.Set;

@Resource
@Entity(name = "dummyEntityB")
public class DummyEntityB {


    @Id
    @Getter
    @Setter
    private Long id;

    @ManyToMany(mappedBy = "dummyEntityBSet")
    private Set<DummyEntityA> dummyEntityASet;


}
