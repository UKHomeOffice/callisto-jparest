package uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities;

import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.Set;

/**
 * A dummy entity that does not extend BaseEntity which also acts as a related entity
 */

@Resource(path = "dummyEntityHs")
@Entity(name = "dummyEntityH")
public class DummyEntityH  {

    @Id
    Long id;

    @ManyToMany(mappedBy = "dummyEntityHSet")
    private Set<DummyEntityG> dummyEntityGSet;

}
