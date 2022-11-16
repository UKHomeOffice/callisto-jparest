package uk.gov.homeoffice.digital.sas.cucumberjparesttestapi.models;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vladmihalcea.hibernate.type.json.JsonType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;

@TypeDef(name = "json", typeClass = JsonType.class)
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

    @Type(type = "json")
    @Column(name = "props", columnDefinition = "json")
    private Map<String, Object> props = new HashMap<>();
}
