package uk.gov.homeoffice.digital.sas.jparest.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityA;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityB;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityC;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityD;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityF;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityG;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityH;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Map;


@SpringBootTest
@ContextConfiguration(locations = "/test-context.xml")
class BaseEntityCheckerServiceTest {

  @PersistenceContext
  private EntityManager entityManager;

  private BaseEntityCheckerService baseEntityCheckerService;

  @BeforeEach
  void setup() {
    baseEntityCheckerService = new BaseEntityCheckerService(entityManager);
  }


  @Test
  void filterBaseEntitySubClasses_notAllEntitiesAreBaseEntitySubclasses_onlyBaseEntitySubclassesAreReturned() {

    Map<Class<?>, String> baseEntitySubclasses =
        baseEntityCheckerService.filterBaseEntitySubClasses();

    assertThat(baseEntitySubclasses).containsKeys(
        DummyEntityA.class,
        DummyEntityB.class,
        DummyEntityC.class,
        DummyEntityD.class,
        DummyEntityF.class,
        DummyEntityG.class)
      .doesNotContainKey(DummyEntityH.class);
  }



}
