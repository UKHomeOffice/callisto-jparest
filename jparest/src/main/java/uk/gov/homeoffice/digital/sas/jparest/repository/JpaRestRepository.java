package uk.gov.homeoffice.digital.sas.jparest.repository;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.expression.spel.standard.SpelExpression;

@NoRepositoryBean
public interface JpaRestRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {

  List<T> findAllByTenantId(UUID tenantId, SpelExpression filter, Pageable pageable);

  Optional<T> findByIdAndTenantId(ID id, UUID tenantId, String include);

  Optional<T> findByIdAndTenantId(ID id, UUID tenantId);

  List<?> findAllByIdAndRelationAndTenantId(ID id,
                                            String relation,
                                            Class<?> relatedEntityType,
                                            UUID tenantId,
                                            SpelExpression filter,
                                            Pageable pageable);

  Long countAllByRelationAndTenantId(Class<?> relatedEntityType,
                                     Collection<Serializable> relatedIds,
                                     UUID tenantId);

  void deleteByIdAndTenantId(ID id, UUID tenantId);
}
