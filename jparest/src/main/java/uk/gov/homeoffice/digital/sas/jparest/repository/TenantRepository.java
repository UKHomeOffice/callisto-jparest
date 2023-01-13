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

  Optional<T> findByIdAndTenantId(UUID id, UUID tenantId);

  Optional<T> findByIdAndTenantId(UUID id, UUID tenantId, String relatedResourceType);

  List<?> findAllByIdAndRelationAndTenantId(UUID tenantId,
                                            UUID id,
                                            String relatedResourceType,
                                            Class<?> relatedEntityClass,
                                            SpelExpression filter,
                                            Pageable pageable);

  Long countAllByRelationAndTenantId(UUID tenantId,
                                     Class<?> relatedEntityClass,
                                     Collection<UUID> relatedIds);

  int deleteByIdAndTenantId(UUID tenantId, UUID id);
}
