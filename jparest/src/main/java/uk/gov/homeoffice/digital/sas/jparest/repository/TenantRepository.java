package uk.gov.homeoffice.digital.sas.jparest.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.expression.spel.standard.SpelExpression;

@NoRepositoryBean
public interface TenantRepository<T> extends JpaRepository<T, UUID> {


  List<T> findAllByTenantId(UUID tenantId, SpelExpression filter, Pageable pageable);

  Optional<T> findByTenantIdAndId(UUID tenantId, UUID id);

  Optional<T> findByTenantIdAndId(UUID tenantId, UUID id, String relatedResourceType);

  @SuppressWarnings("squid:S1452") // Generic wildcard types should not be used in return parameters
  List<?> findAllByTenantIdAndIdAndRelation(UUID tenantId,
                                            UUID id,
                                            String relatedResourceType,
                                            Class<?> relatedEntityClass,
                                            SpelExpression filter,
                                            Pageable pageable);

  Long countAllByTenantIdAndRelation(UUID tenantId,
                                     Class<?> relatedEntityClass,
                                     Collection<UUID> relatedIds);

  void deleteByTenantIdAndId(UUID tenantId, UUID id);

  UUID findId(T entity);
}
