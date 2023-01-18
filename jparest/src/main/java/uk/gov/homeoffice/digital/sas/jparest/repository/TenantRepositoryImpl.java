package uk.gov.homeoffice.digital.sas.jparest.repository;

import java.util.UUID;
import javax.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;


/**
 * Repository that queries entities and has common functionality for paging, sorting
 * and filtering resources.
 * <p>
 * Resources for ManyToMany relationships can also be queried.
 */
public class TenantRepositoryImpl<T>
    extends SimpleJpaRepository<T, UUID> implements TenantRepository<T> {

  public TenantRepositoryImpl(Class<T> entityType, EntityManager entityManager) {
    super(entityType, entityManager);
  }

  //TODO EAHW-2543: Implement repository methods
}
