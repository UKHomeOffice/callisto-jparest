package uk.gov.homeoffice.digital.sas.jparest.repository;

import java.io.Serializable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface JpaRestRepository<T, Y extends Serializable> extends JpaRepository<T, Y> {

  //TODO EAHW-2543: Add repository methods
}
