package uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;

import java.util.UUID;

@Resource
@Entity(name = "dummyEntityC")
public class DummyEntityC extends BaseEntity {

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
    private String description;

    @Getter
    @Setter
    private Long index;

}
