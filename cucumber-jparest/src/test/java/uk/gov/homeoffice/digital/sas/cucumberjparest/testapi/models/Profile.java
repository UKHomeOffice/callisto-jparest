package uk.gov.homeoffice.digital.sas.cucumberjparest.testapi.models;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import com.vladmihalcea.hibernate.type.json.JsonType;
import org.springframework.format.annotation.DateTimeFormat;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;

@Resource(path = "profiles")
@Entity(name = "profiles")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class Profile extends BaseEntity {

    @NotEmpty
    private String preferences;

    @NotEmpty
    private String bio;

    @NotEmpty
    private String phoneNumber;

    @NotNull
    @Column(name = "dob", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
//    private Date dob;
    private Instant dob;

    @NotNull
    @Column(name = "first_release", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
//    private Date firstRelease;
    private Instant firstRelease;

    @Type(JsonType.class)
    @Column(name = "props", columnDefinition = "json")
    private Map<String, Object> props = new HashMap<>();
}
