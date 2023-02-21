package uk.gov.homeoffice.digital.sas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.homeoffice.digital.sas.model.Profile;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, String> {
}
