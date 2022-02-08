package uk.gov.homeoffice.digital.sas.demo.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;

@Resource
@Entity(name = "sessions")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Session {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long session_id;
    private String session_name;
    private String session_description;
    private Integer session_length;
    
    // @ManyToMany
    // @JoinTable(
    //     name = "session_speakers",
    //     joinColumns = @JoinColumn(name = "session_id"),
    //     inverseJoinColumns = @JoinColumn(name = "speaker_id")
    // )
    // private List<Speaker> speakers;

    public Session() {
        
    }


    public Long getSession_id() {
        return this.session_id;
    }

    public void setSession_id(Long session_id) {
        this.session_id = session_id;
    }

    public String getSession_name() {
        return this.session_name;
    }

    public void setSession_name(String session_name) {
        this.session_name = session_name;
    }

    public String getSession_description() {
        return this.session_description;
    }

    public void setSession_description(String session_description) {
        this.session_description = session_description;
    }

    public Integer getSession_length() {
        return this.session_length;
    }

    public void setSession_length(Integer session_length) {
        this.session_length = session_length;
    }

    // public List<Speaker> getSpeakers() {
    //     return this.speakers;
    // }

    // public void setSpeakers(List<Speaker> speakers) {
    //     this.speakers = speakers;
    // }

}
