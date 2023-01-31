package uk.gov.homeoffice.digital.sas.jparest.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.homeoffice.digital.sas.jparest.config.BaseEntityCheckerServiceTestConfig;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.*;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;


@SpringBootTest
@ContextConfiguration(locations = "/test-context.xml", classes = BaseEntityCheckerServiceTestConfig.class)
class BaseEntityCheckerServiceTest {

  @Autowired
  private BaseEntityCheckerService baseEntityCheckerService;


  @Test
  void filterBaseEntitySubClasses_notAllEntitiesAreBaseEntitySubclasses_onlyBaseEntitySubclassesAreReturned() {

    Map<Class<?>, String> actualBaseEntitySubclassesMap =
        baseEntityCheckerService.getBaseEntitySubClasses();

    var expectedBaseEntitySubclasses = Set.of(
            DummyEntityA.class,
            DummyEntityB.class,
            DummyEntityC.class,
            DummyEntityD.class,
            DummyEntityF.class,
            DummyEntityG.class);

    assertThat(expectedBaseEntitySubclasses).allSatisfy(subclass ->
            assertThat(subclass.getSuperclass()).isEqualTo(BaseEntity.class));
    assertThat(DummyEntityE.class.getSuperclass()).isEqualTo(DummyEntityD.class);
    assertThat(DummyEntityH.class.getSuperclass()).isEqualTo(Object.class);

    assertThat(actualBaseEntitySubclassesMap.keySet()).containsAll(expectedBaseEntitySubclasses);
    assertThat(actualBaseEntitySubclassesMap)
            .containsKey(DummyEntityE.class)
            .doesNotContainKey(DummyEntityH.class);
  }

}
