package uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities;

import lombok.Getter;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;

import javax.persistence.Id;

@Resource
public class DummyEntityC {


    @Id
    @Getter
    private Long id;


}
