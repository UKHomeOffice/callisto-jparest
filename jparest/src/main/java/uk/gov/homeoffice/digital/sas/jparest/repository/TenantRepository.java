package uk.gov.homeoffice.digital.sas.jparest.repository;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.expression.spel.standard.SpelExpression;

@NoRepositoryBean
public interface TenantRepository<T, Y extends Serializable> extends JpaRepository<T, Y> {


  List<T> findAllByTenantId(UUID tenantId, SpelExpression filter, Pageable pageable);

  Optional<T> findByIdAndTenantId(Y id, UUID tenantId);
}
