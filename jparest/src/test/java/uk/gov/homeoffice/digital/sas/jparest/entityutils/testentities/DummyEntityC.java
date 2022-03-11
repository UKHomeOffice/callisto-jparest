package uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities;

import lombok.Getter;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;

import javax.persistence.Entity;
import javax.persistence.Id;

@Resource(path = "dummyEntityCs")
@Entity(name = "dummyEntityC")
public class DummyEntityC {


    @Id
    @Getter
    private Long id;


}
