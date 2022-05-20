package uk.gov.homeoffice.digital.sas.demo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotEmpty;

@Resource(path = "records")
@Entity(name = "records")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@NoArgsConstructor @Getter @Setter
public class Record extends BaseEntity {

    @NotEmpty
    private String record_name;

    @ManyToOne
    @JoinColumn(name="artist_id", nullable=false, updatable=false)
    @JsonIgnore
    private Artist artist;
}
