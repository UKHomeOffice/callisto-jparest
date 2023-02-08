package uk.gov.homeoffice.digital.sas.cucumberjparest.testapi.models;

import java.util.Date;
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
    private Date dob;

    @NotNull
    private Date firstRelease;

    @Type(JsonType.class)
    @Column(name = "props", columnDefinition = "json")
    private Map<String, Object> props = new HashMap<>();
}
