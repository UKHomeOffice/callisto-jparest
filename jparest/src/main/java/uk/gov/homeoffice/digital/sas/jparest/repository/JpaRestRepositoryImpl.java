package uk.gov.homeoffice.digital.sas.jparest.repository;

import java.io.Serializable;
import javax.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;


/**
 * Repository that queries entities and has common functionality for paging, sorting
 * and filtering resources.
 * <p>
 * Resources for ManyToMany relationships can also be queried.
 */
public class JpaRestRepositoryImpl<T, Y extends Serializable>
        extends SimpleJpaRepository<T, Y> implements JpaRestRepository<T, Y> {

  public JpaRestRepositoryImpl(Class<T> entityType, EntityManager entityManager) {
    super(entityType, entityManager);
  }

  //TODO EAHW-2543: Implement repository methods
}
