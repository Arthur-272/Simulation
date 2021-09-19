package com.pointbasis.simulation.service.impl;

import com.pointbasis.simulation.domain.Project;
import com.pointbasis.simulation.domain.User;
import com.pointbasis.simulation.repository.GeometryRepository;
import com.pointbasis.simulation.repository.ProjectRepository;
import com.pointbasis.simulation.repository.UserRepository;
import com.pointbasis.simulation.service.ProjectService;
import com.pointbasis.simulation.service.UserService;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tech.jhipster.web.util.HeaderUtil;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final Logger log = LoggerFactory.getLogger(ProjectServiceImpl.class);

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GeometryRepository modelRepository;

    @Autowired
    private GeometryServiceImpl modelService;

    @Autowired
    private MeshServiceImpl meshService;

    @Autowired
    private UserService userService;

    @Override
    public void deleteProject(String id) throws Exception {
        Optional<Project> project = projectRepository.findById(id);
        if (project.isPresent()) {
            //            Deleting the project in the list of the user's project
            String userId = project.get().getUser().getId();
            ObjectId projectId = new ObjectId(id);
            modelService.deleteGeometriesByProjectId(projectId);
            meshService.deleteMeshesByProjectId(projectId);

            String path = userId + "/" + id;
            modelService.deleteObjectsFromS3(path);
            projectRepository.deleteById(id);
        } else {
            throw new Exception("Project not found");
        }
    }

    @Override
    public List<Project> getAllProjectsByUser() throws Exception {
        Optional<User> user = userRepository.findOneByLogin(userService.getUserWithAuthorities().get().getLogin());
        if (user.isPresent()) {
            ObjectId userId = new ObjectId(user.get().getId());
            return projectRepository.getAllProjectsByUser(userId);
        } else {
            throw new Exception("User not found");
        }
    }

    @Override
    public ResponseEntity<Project> createProject(Project project) throws Exception {
        Optional<User> user = userRepository.findOneByLogin(userService.getUserWithAuthorities().get().getLogin());
        if (user.isPresent()) {
            ObjectId projectId = new ObjectId();
            project.setId(projectId.toHexString());
            project.setUser(user.get());
            Project result = projectRepository.save(project);

            List<Project> list = user.get().getProjects();
            list.add(project);
            user.get().setProjects(list);
            userRepository.save(user.get());

            return ResponseEntity
                .created(new URI("/api/projects/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, "project", result.getId()))
                .body(result);
        } else {
            throw new Exception("User not found");
        }
    }
}
