package com.pointbasis.simulation.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * A Mesh.
 */
@Document(collection = "mesh")
public class Mesh implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Field("outputLink")
    private Map<String, String> outputLink = null;

    @Field("model")
    @DBRef
    private Geometry model;

    @Field("project")
    @DBRef
    private Project project;

    @Field("edge_length")
    private Double edgeLength;

    @Field("epsilon")
    private Double tolerance;

    @Field("simulated")
    private boolean hasBeenSimulated;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Mesh id(String id) {
        this.id = id;
        return this;
    }

    public Map<String, String> getOutputLink() {
        return outputLink;
    }

    public void setOutputLink(Map<String, String> outputLink) {
        this.outputLink = outputLink;
    }

    public Geometry getModel() {
        return model;
    }

    public void setModel(Geometry model) {
        this.model = model;
    }

    @JsonIgnore
    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Double getEdgeLength() {
        return this.edgeLength;
    }

    public Mesh edgeLength(Double edgeLength) {
        this.edgeLength = edgeLength;
        return this;
    }

    public void setEdgeLength(Double edgeLength) {
        this.edgeLength = edgeLength;
    }

    public Double getTolerance() {
        return this.tolerance;
    }

    public Mesh epsilon(Double epsilon) {
        this.tolerance = epsilon;
        return this;
    }

    public void setTolerance(Double tolerance) {
        this.tolerance = tolerance;
    }

    public boolean isHasBeenSimulated() {
        return hasBeenSimulated;
    }

    public void setHasBeenSimulated(boolean hasBeenSimulated) {
        this.hasBeenSimulated = hasBeenSimulated;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Mesh)) {
            return false;
        }
        return id != null && id.equals(((Mesh) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Mesh{" +
            "id=" + getId() +
            ", edgeLength=" + getEdgeLength() +
            ", epsilon=" + getTolerance() +
            "}";
    }
}
