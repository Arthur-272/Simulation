package com.pointbasis.simulation.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * A Geometry.
 */
@Document(collection = "geometry")
public class Geometry implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Field("name")
    private String name;

    @Field("inputLink")
    private Map<String, String> inputLink = null;

    @Field("project")
    @DBRef
    private Project project;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Geometry id(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public Geometry name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getInputLink() {
        return inputLink;
    }

    public void setInputLink(Map<String, String> inputLink) {
        this.inputLink = inputLink;
    }

    @JsonIgnore
    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Geometry)) {
            return false;
        }
        return id != null && id.equals(((Geometry) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
