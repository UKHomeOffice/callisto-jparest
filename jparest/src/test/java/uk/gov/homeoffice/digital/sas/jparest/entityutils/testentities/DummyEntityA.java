package uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities;

import java.util.Set;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;

@Resource(path = "dummyEntityAs")
@Entity(name = "dummyEntityA")
public class DummyEntityA extends BaseEntity {

    @Id
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    @Type(type="uuid-char")
    private UUID tenant_id;

    @Getter
    @Setter
    @ManyToMany
    @JoinTable(name = "dummyEntityA_dummyEntityB", joinColumns = @JoinColumn(name = "dummyEntityA"), inverseJoinColumns = @JoinColumn(name = "dummyEntityB"))
    private Set<DummyEntityB> dummyEntityBSet;

}
