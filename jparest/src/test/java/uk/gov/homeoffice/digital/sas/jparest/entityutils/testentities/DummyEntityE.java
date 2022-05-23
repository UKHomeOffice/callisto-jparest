package uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities;

import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

@Entity(name = "dummyEntityE")
public class DummyEntityE extends BaseEntity implements Serializable {

    @Id
    private Long id1;


    @Id
    private Long id2;

}