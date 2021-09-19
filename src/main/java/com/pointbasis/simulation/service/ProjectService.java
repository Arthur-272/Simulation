package com.pointbasis.simulation.service;

import com.pointbasis.simulation.domain.Project;
import java.util.List;
import org.springframework.http.ResponseEntity;

public interface ProjectService {
    void deleteProject(String id) throws Exception;

    List<Project> getAllProjectsByUser() throws Exception;

    ResponseEntity<Project> createProject(Project project) throws Exception;
}
