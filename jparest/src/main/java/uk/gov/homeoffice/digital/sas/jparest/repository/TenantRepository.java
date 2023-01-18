package uk.gov.homeoffice.digital.sas.jparest.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface TenantRepository<T> extends JpaRepository<T, UUID> {

  //TODO EAHW-2543: Add repository methods
}
