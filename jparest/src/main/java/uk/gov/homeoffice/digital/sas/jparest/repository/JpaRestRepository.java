package uk.gov.homeoffice.digital.sas.jparest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface JpaRestRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {

  //TODO EAHW-2543: Add repository methods
}
