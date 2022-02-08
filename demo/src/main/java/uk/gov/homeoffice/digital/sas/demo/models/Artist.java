package uk.gov.homeoffice.digital.sas.demo.models;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Resource(path = "artists")
@Entity(name = "artists")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@NoArgsConstructor @Getter @Setter
public class Artist extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long artist_id;
    private Long profile_id;
    private String performance_name;

    @OneToOne(optional=false)
    @JoinColumn(name="profile_id", unique=true, nullable=false, insertable=false, updatable=false)
//    @JsonIgnore
    private Profile profile;

    @OneToMany(mappedBy="artist")
    private Set<Record> records;

    @ManyToMany(mappedBy = "artists")
    private Set<Concert> concerts; 

}
