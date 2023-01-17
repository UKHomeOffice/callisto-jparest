package uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities;

import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public final class DummyEntityTestUtil {


  private DummyEntityTestUtil() {
    //no instantiation
  }


  public static Predicate<Class<?>> getBaseEntitySubclassPredicate() {
    Set<Class<?>> baseEntitySubClasses = Set.of(
            DummyEntityA.class,
            DummyEntityB.class,
            DummyEntityC.class,
            DummyEntityD.class,
            DummyEntityF.class,
            DummyEntityG.class
    );
    return baseEntitySubClasses::contains;
  }

  public static <T extends BaseEntity> T getResource(Class<?> resourceClass) {
    try {
      return (T) resourceClass.getConstructor().newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Error creating object of resource type: " + resourceClass.getSimpleName());
    }
  }

  public static <T extends BaseEntity> T getResource(Class<?> resourceClass,
                                                     UUID resourceId,
                                                     UUID tenantId) {
    T resource = getResource(resourceClass);
    resource.setId(resourceId);
    resource.setTenantId(tenantId);
    return resource;
  }

}
