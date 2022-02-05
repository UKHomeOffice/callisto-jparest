package uk.gov.homeoffice.digital.sas.demo.repositories;

import uk.gov.homeoffice.digital.sas.demo.models.Session;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Long> {
    
}
