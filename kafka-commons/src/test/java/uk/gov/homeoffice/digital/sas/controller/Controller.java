package uk.gov.homeoffice.digital.sas.controller;

import io.netty.util.concurrent.ProgressiveFuture;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.gov.homeoffice.digital.sas.model.Profile;
import uk.gov.homeoffice.digital.sas.repository.ProfileRepository;

@org.springframework.stereotype.Controller
@RequestMapping("profiles")
public class Controller {

  @Autowired
  private ProfileRepository repository;

  @PostMapping("/{id}")
  public Profile postProfile(@RequestBody Profile profile, @PathVariable Long id) {
    return repository.saveAndFlush(profile);
  }

  @PutMapping("/{id}")
  public Profile putProfile(@RequestBody Profile profile, @PathVariable Long id) {
    return repository.saveAndFlush(profile);
  }

  @DeleteMapping("/{id}")
  public void deleteProfile(@RequestBody Profile profile, @PathVariable Long id) {
    repository.delete(profile);
  }


}
