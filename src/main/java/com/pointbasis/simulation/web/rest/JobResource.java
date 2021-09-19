package com.pointbasis.simulation.web.rest;

import com.pointbasis.simulation.domain.Job;
import com.pointbasis.simulation.repository.JobRepository;
import com.pointbasis.simulation.service.Services;
import com.pointbasis.simulation.service.impl.JobServiceImpl;
import com.pointbasis.simulation.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.pointbasis.simulation.domain.Job}.
 */
@RestController
@RequestMapping("/api")
public class JobResource {

    private final Logger log = LoggerFactory.getLogger(JobResource.class);

    private static final String ENTITY_NAME = "job";

    @Autowired
    private JobServiceImpl jobService;

    @Autowired
    private Services services;

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final JobRepository jobRepository;

    @GetMapping("/getJobStatus")
    public ResponseEntity getJobStatus(@RequestParam String jobId) throws Exception {
        log.debug("REST request to get JobStatus : {}", jobId);
        String ip = services.getIP();
        String status = jobService.getJobStatus(jobId, ip);
        return ResponseEntity.ok().body(status);
    }

    @GetMapping("/cancelJob")
    public ResponseEntity cancelJob(@RequestParam String jobId) {
        log.debug("REST request to cancelJob : {}", jobId);
        return jobService.cancelJob(jobId);
    }

    @GetMapping("/project/{projectId}/jobs")
    public List<Job> getJobByProjectId(@PathVariable String projectId) throws Exception {
        log.debug("REST request to get getJobByProjectId : {}", projectId);
        return jobService.getJobsByProjectId(projectId);
    }

    @GetMapping("updateJobStatus")
    public void updateJobStatus() throws Exception {
        log.debug("REST request to get updateJobStatus : ");
        jobService.updateJobStatus();
    }

    public JobResource(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    /**
     * {@code POST  /jobs} : Create a new job.
     *
     * @param job the job to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new job, or with status {@code 400 (Bad Request)} if the job has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/jobs")
    public ResponseEntity<Job> createJob(@RequestBody Job job) throws URISyntaxException {
        log.debug("REST request to save Job : {}", job);
        if (job.getId() != null) {
            throw new BadRequestAlertException("A new job cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Job result = jobRepository.save(job);
        return ResponseEntity
            .created(new URI("/api/jobs/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId()))
            .body(result);
    }

    /**
     * {@code PUT  /jobs/:id} : Updates an existing job.
     *
     * @param id the id of the job to save.
     * @param job the job to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated job,
     * or with status {@code 400 (Bad Request)} if the job is not valid,
     * or with status {@code 500 (Internal Server Error)} if the job couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/jobs/{id}")
    public ResponseEntity<Job> updateJob(@PathVariable(value = "id", required = false) final String id, @RequestBody Job job)
        throws URISyntaxException {
        log.debug("REST request to update Job : {}, {}", id, job);
        if (job.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, job.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!jobRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Job result = jobRepository.save(job);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, job.getId()))
            .body(result);
    }

    /**
     * {@code PATCH  /jobs/:id} : Partial updates given fields of an existing job, field will ignore if it is null
     *
     * @param id the id of the job to save.
     * @param job the job to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated job,
     * or with status {@code 400 (Bad Request)} if the job is not valid,
     * or with status {@code 404 (Not Found)} if the job is not found,
     * or with status {@code 500 (Internal Server Error)} if the job couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/jobs/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<Job> partialUpdateJob(@PathVariable(value = "id", required = false) final String id, @RequestBody Job job)
        throws URISyntaxException {
        log.debug("REST request to partial update Job partially : {}, {}", id, job);
        if (job.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, job.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!jobRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Job> result = jobRepository
            .findById(job.getId())
            .map(
                existingJob -> {
                    if (job.getJobId() != null) {
                        existingJob.setJobId(job.getJobId());
                    }
                    if (job.getJobStatus() != null) {
                        existingJob.setJobStatus(job.getJobStatus());
                    }
                    if (job.getnCPUs() != 0) {
                        existingJob.setnCPUs(job.getnCPUs());
                    }
                    if (job.getElapsedTime() != null) {
                        existingJob.setElapsedTime(job.getElapsedTime());
                    }
                    if (job.getDatetime() != 0) {
                        existingJob.setDatetime(job.getDatetime());
                    }

                    return existingJob;
                }
            )
            .map(jobRepository::save);

        return ResponseUtil.wrapOrNotFound(result, HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, job.getId()));
    }

    /**
     * {@code GET  /jobs} : get all the jobs.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of jobs in body.
     */
    @GetMapping("/jobs")
    public List<Job> getAllJobs() {
        log.debug("REST request to get all Jobs");
        return jobRepository.findAll();
    }

    /**
     * {@code GET  /jobs/:id} : get the "id" job.
     *
     * @param id the id of the job to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the job, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/jobs/{id}")
    public ResponseEntity<Job> getJob(@PathVariable String id) {
        log.debug("REST request to get Job : {}", id);
        Optional<Job> job = jobRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(job);
    }

    /**
     * {@code DELETE  /jobs/:id} : delete the "id" job.
     *
     * @param jobId the id of the job to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/jobs/{jobId}")
    public ResponseEntity deleteJob(@PathVariable String jobId) throws Exception {
        log.debug("REST request to delete deleteJob : {}", jobId);
        return jobService.deleteJob(jobId, services.getIP());
    }
}
