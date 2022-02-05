package uk.gov.homeoffice.digital.sas.demo.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import uk.gov.homeoffice.digital.sas.jparest.Resource;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Resource(path = "records")
@Entity(name = "records")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@NoArgsConstructor @Getter @Setter
public class Record extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long record_id;
    private Long artist_id;
    private String record_name;

    @ManyToOne
    @JoinColumn(name="artist_id", nullable=false, insertable=false, updatable=false)
    private Artist artist;
}
