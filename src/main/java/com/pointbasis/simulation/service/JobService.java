package com.pointbasis.simulation.service;

import com.pointbasis.simulation.domain.Geometry;
import com.pointbasis.simulation.domain.Job;
import com.pointbasis.simulation.service.dto.GeometryDTO;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;

public interface JobService {
    String getJobStatus(String jobId, String ip);

    ResponseEntity cancelJob(String jobId);

    ResponseEntity deleteJob(String jobId, String ip) throws Exception;

    void updateJobStatus();

    List<Job> getJobsByProjectId(String projectId) throws Exception;

    Job doJob(Geometry model, String edgeLength, String tolerance, String ip);

    ResponseEntity simulate(String projectId, String modelId, GeometryDTO data);

    void completeJob(Job jobId, String ip);

    Map<String, String> getJobCompletionDetails(String jobId, String ip) throws Exception;
}
