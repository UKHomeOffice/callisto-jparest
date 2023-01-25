package uk.gov.homeoffice.digital.sas.jparest.service;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import org.springframework.stereotype.Service;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;

@Service
public class BaseEntityCheckerService {

  private final EntityManager entityManager;

  public BaseEntityCheckerService(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  /**
   * Filters all the entities and returns a map of only the BaseEntity subclasses
   *
   * @return A map of the key as the entity java class and the value as the entity name
   */
  public Map<Class<?>, String> filterBaseEntitySubClasses() {
    return entityManager.getMetamodel().getEntities().stream()
        .filter(entityType -> entityType.getJavaType().isAnnotationPresent(Resource.class)
            && classHasBaseEntityParent(entityType.getJavaType()))
        .collect(Collectors.toMap(EntityType::getJavaType, EntityType::getName));
  }

  private boolean classHasBaseEntityParent(Class<?> childClass) {
    var superType = childClass.getSuperclass();
    while (!superType.equals(Object.class)) {
      if (superType.equals(BaseEntity.class)) {
        return true;
      }
      superType = superType.getSuperclass();
    }
    return false;
  }

  public Predicate<Class<?>> isBaseEntitySubclass(
      Map<Class<?>, String> baseEntitySubClassesMap) {
    return baseEntitySubClassesMap::containsKey;
  }

}
