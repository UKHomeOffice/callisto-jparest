package uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities;

import java.util.Set;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;

@Resource(path = "dummyEntityBs")
@Entity(name = "dummyEntityB")
public class DummyEntityB extends BaseEntity {

    @Id
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    @Type(type="uuid-char")
    private UUID tenant_id;

    @ManyToMany(mappedBy = "dummyEntityBSet")
    private Set<DummyEntityA> dummyEntityASet;

}
