package uk.gov.homeoffice.digital.sas.jparest.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.expression.spel.standard.SpelExpression;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;



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

    @Override
    public List<T> findAllByTenantId(UUID tenantId, SpelExpression filter, Pageable pageable) {
        throw new RuntimeException("Jpa Respository Method Not Yet implemented");
    }

    @Override
    public Optional<T> findByIdAndTenantId(ID id, UUID tenantId, String include) {
        throw new RuntimeException("Jpa Respository Method Not Yet implemented");
    }

    @Override
    public Optional<T> findByIdAndTenantId(ID id, UUID tenantId) {
        throw new RuntimeException("Jpa Respository Method Not Yet implemented");
    }

    @Override
    public List<?> findAllByIdAndRelationAndTenantId(ID id, String relation, Class<?> relatedEntityType, UUID tenantId, SpelExpression filter, Pageable pageable) {
        throw new RuntimeException("Jpa Respository Method Not Yet implemented");
    }

    @Override
    public Long countAllByRelationAndTenantId(Class<?> relatedEntityType, Collection<Serializable> relatedIds, UUID tenantId) {
        throw new RuntimeException("Jpa Respository Method Not Yet implemented");
    }

    @Override
    public void deleteByIdAndTenantId(ID id, UUID tenantId) {
        throw new RuntimeException("Jpa Respository Method Not Yet implemented");
    }
}
