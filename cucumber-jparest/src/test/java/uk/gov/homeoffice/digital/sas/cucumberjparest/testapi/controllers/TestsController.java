package uk.gov.homeoffice.digital.sas.cucumberjparest.testapi.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tests")
public class TestsController {
    
    @GetMapping("empty.html")
    public @ResponseBody ResponseEntity<String> getEmptyResponse() {
        return new ResponseEntity<>(HttpStatus.OK);
    }

}

