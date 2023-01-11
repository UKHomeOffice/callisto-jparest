package uk.gov.homeoffice.digital.sas.jparest.repository;

import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import javax.persistence.EntityManager;
import java.io.Serializable;


/**
 * Repository that queries entities and has common functionality for paging, sorting
 * and filtering resources.
 * <p>
 * Resources for ManyToMany relationships can also be queried.
 */
public class JpaRestRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements JpaRestRepository<T, ID> {

    public JpaRestRepositoryImpl(Class<T> entityType, EntityManager entityManager) {
        super(entityType, entityManager);
    }

    //TODO EAHW-2543: Implement repository methods
}
