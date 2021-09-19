package com.pointbasis.simulation.domain;

import java.io.Serializable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * A Job.
 */
@Document(collection = "job")
public class Job implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Field("jobId")
    private String jobId;

    @Field("user")
    @DBRef
    private User user;

    @Field("project")
    @DBRef
    private Project project;

    @Field("mesh")
    @DBRef
    private Mesh mesh;

    @Field("status")
    private String jobStatus;

    @Field("nCPUs")
    private int nCPUs;

    @Field("elapsedTime")
    private String elapsedTime;

    @Field("date")
    private long datetime;

    public Job() {}

    public Job(String id, String jobId, Project project, Mesh mesh, String jobStatus) {
        this.id = id;
        this.jobId = jobId;
        this.project = project;
        this.mesh = mesh;
        this.jobStatus = jobStatus;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
    }

    public String getJobStatus() {
        return jobStatus;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }

    public int getnCPUs() {
        return nCPUs;
    }

    public void setnCPUs(int nCPUs) {
        this.nCPUs = nCPUs;
    }

    public String getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(String elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public long getDatetime() {
        return datetime;
    }

    public void setDatetime(long datetime) {
        this.datetime = datetime;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Job)) {
            return false;
        }
        return id != null && id.equals(((Job) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }
}
