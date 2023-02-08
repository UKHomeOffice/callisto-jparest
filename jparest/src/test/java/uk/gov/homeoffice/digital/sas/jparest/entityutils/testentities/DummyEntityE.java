package uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities;

import io.swagger.v3.oas.annotations.media.ExampleObject;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;

import jakarta.persistence.Entity;


//A dummy test entity extending another DummyEntity
// to prove BaseEntity subclasses can be discovered at deeper nested levels

@Resource(filterExamples = {@ExampleObject})
@Entity(name = "dummyEntityE")
public class DummyEntityE extends DummyEntityD {

}
