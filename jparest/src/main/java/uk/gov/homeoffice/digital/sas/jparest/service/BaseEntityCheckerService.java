package uk.gov.homeoffice.digital.sas.jparest.service;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.stereotype.Service;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;

@Service
public class BaseEntityCheckerService {

  private final EntityManager entityManager;

  /**
   * A map containing resource annotated classes that inherit from BaseEntity as the key
   * and their entity type name as the value
   */
  @Getter
  private Map<Class<?>, String> baseEntitySubClasses;

  public BaseEntityCheckerService(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  /**
   * Filters all the entities and retains only the BaseEntity subclasses
   * Stores a map of the key as the entity java class and the value as the entity name
   *
   * Note: If we get to a point where client users of this library
   *  begin to have a huge number of entities it may be worth revisiting this and
   *  deleting the stored baseEntitySubClasses once all app config has completed
   */
  @PostConstruct
  private void setBaseEntitySubClasses() {
    this.baseEntitySubClasses = entityManager.getMetamodel().getEntities().stream()
            .filter(entityType -> isResourceEntity(entityType.getJavaType()))
            .collect(Collectors.toUnmodifiableMap(EntityType::getJavaType, EntityType::getName));
  }

  private boolean isResourceEntity(Class<?> childClass) {
    return childClass.isAnnotationPresent(Resource.class)
            && BaseEntity.class.isAssignableFrom(childClass);
  }

  public boolean isBaseEntitySubclass(Class<?> resourceClass) {
    return baseEntitySubClasses.containsKey(resourceClass);
  }

}
