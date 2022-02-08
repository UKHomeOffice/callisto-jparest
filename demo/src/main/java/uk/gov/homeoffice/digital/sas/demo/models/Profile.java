package uk.gov.homeoffice.digital.sas.demo.models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Resource(path = "profiles")
@Entity(name = "profiles")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@NoArgsConstructor @Getter @Setter
public class Profile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long profile_id;
    private String preferences;
    private String bio;
    private String phone_number;
    private Date dob;


}
