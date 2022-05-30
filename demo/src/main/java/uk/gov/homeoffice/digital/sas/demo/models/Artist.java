package uk.gov.homeoffice.digital.sas.demo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.Set;
import java.util.UUID;

@Resource(path = "artists", filterExamples = {
    @ExampleObject(name = "performance_name match", value = "performance_name matches '%the%'"),
    @ExampleObject(name = "artist_id less than", value = "artisit_id < 5"),
    @ExampleObject(name = "artist_id less than or eaqual to", value = "artisit_id <= 10")
})
@Entity(name = "artists")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@NoArgsConstructor @Getter @Setter
public class Artist extends BaseEntity {

    @Type(type="uuid-char")
    @Column(name = "profile_id")
    private UUID profileId;

    @NotEmpty
    private String performance_name;

    @OneToOne(optional=false)
    @JoinColumn(name="profile_id", unique=true, nullable=false, insertable=false, updatable=false)
    @JsonIgnore
    private Profile profile;

    @OneToMany(mappedBy="artist")
    @JsonIgnore
    private Set<Record> records;

    @ManyToMany(mappedBy = "artists")
    @JsonIgnore
    private Set<Concert> concerts; 

}
