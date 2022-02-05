package uk.gov.homeoffice.digital.sas.demo.controllers;

import java.util.List;

import uk.gov.homeoffice.digital.sas.demo.models.Session;
import uk.gov.homeoffice.digital.sas.demo.repositories.SessionRepository;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sessions")
public class SessionsController {

    @Autowired
    private SessionRepository sessionRepository; 

    @GetMapping
    public List<Session> list() {
        return sessionRepository.findAll();
    }
    
    @GetMapping
    @RequestMapping("{id}")
    public Session get(@PathVariable Long id) {
        return sessionRepository.getById(id);
    }

    @PostMapping
    public Session create(@RequestBody final Session session) {
        return sessionRepository.saveAndFlush(session);
    }

    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable Long id) {
        sessionRepository.deleteById(id);
    }

    @RequestMapping(value = "{id}", method = RequestMethod.PUT)
    public Session get(@PathVariable Long id, @RequestBody final Session session) {
        Session orig = sessionRepository.getById(id);
        BeanUtils.copyProperties(session, orig, "session_id");
        return sessionRepository.saveAndFlush(orig);
    }
}
