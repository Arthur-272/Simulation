package com.pointbasis.simulation.service.impl;

import static com.pointbasis.simulation.others.Constants.*;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.model.*;
import com.pointbasis.simulation.domain.Geometry;
import com.pointbasis.simulation.domain.Project;
import com.pointbasis.simulation.domain.User;
import com.pointbasis.simulation.repository.GeometryRepository;
import com.pointbasis.simulation.repository.ProjectRepository;
import com.pointbasis.simulation.repository.UserRepository;
import com.pointbasis.simulation.service.GeometryService;
import com.pointbasis.simulation.service.Services;
import com.pointbasis.simulation.service.UserService;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.*;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tech.jhipster.web.util.HeaderUtil;

@Service
public class GeometryServiceImpl implements GeometryService {

    private final Logger log = LoggerFactory.getLogger(GeometryServiceImpl.class);

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private GeometryRepository modelRepository;

    @Autowired
    private Services services;

    @Override
    public ResponseEntity<Geometry> createModel(MultipartFile object, String projectId) throws Exception {
        Optional<User> user = userRepository.findOneByLogin(userService.getUserWithAuthorities().get().getLogin());
        if (user.isPresent()) {
            String userId = user.get().getId();
            Optional<Project> project = projectRepository.findById(projectId);
            if (project.isPresent()) {
                Map<String, String> inputLink = new HashMap<>();
                try {
                    String modelName = object.getOriginalFilename();

                    /**
                     * TODO: Use unique ObjectId instead of modelName to resolve the conflict in multi tenant architecture.
                     * */
                    String filePath = OBJECT_PATH + modelName;
                    File fileSTL = new File(filePath);
                    object.transferTo(fileSTL);
                    File fileDRC = convertToDRC(fileSTL);
                    String stl = userId + "/" + projectId + "/inputFiles/" + modelName;
                    String drc = userId + "/" + projectId + "/inputFiles/" + modelName.split("\\.")[0] + ".drc";

                    s3client.putObject(S3_BUCKET_NAME, stl, fileSTL);
                    fileSTL.delete();

                    s3client.putObject(S3_BUCKET_NAME, drc, fileDRC);
                    fileDRC.delete();

                    inputLink.put("stl", stl);
                    inputLink.put("drc", drc);

                    Geometry model = new Geometry();
                    ObjectId modelId = new ObjectId();
                    model.setId(modelId.toHexString());
                    model.setName(modelName.split("\\.")[0]);
                    model.setInputLink(inputLink);
                    model.setProject(project.get());
                    model.getProject().setUser(user.get());
                    List<Geometry> list = project.get().getModels();
                    list.add(model);
                    project.get().setModels(list);
                    projectRepository.save(project.get());

                    Geometry result = modelRepository.save(model);
                    return ResponseEntity
                        .created(new URI("/api/models/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, "model", result.getId()))
                        .body(result);
                } catch (Exception e) {
                    System.out.println("Error uploading the file");
                    throw new Exception(e.toString());
                }
            } else {
                throw new Exception("Project not found");
            }
        } else {
            throw new Exception("User not found");
        }
    }

    @Override
    public File convertToDRC(File stl) {
        ObjectId temp = new ObjectId();
        String filePathObj = OBJECT_PATH + temp + ".obj";
        String filePathDrc = OBJECT_PATH + temp + ".drc";
        File obj = new File(filePathObj);
        File drc = new File(filePathDrc);
        StringBuilder command = new StringBuilder();

        //            Converting stl to obj
        command
            .append("meshio-convert ")
            .append(stl.getAbsolutePath())
            .append(" ")
            .append(obj.getAbsolutePath())
            .append(" ")
            .append("-i stl -o obj");
        try {
            services.run(command.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        //            Converting obj to drc
        command.setLength(0);
        if (OS.contains("windows")) {
            command.append(DRACO_ENCODER_EXE);
        } else if (OS.contains("linux")) {
            command.append(DRACO_ENCODER);
        } else if (OS.contains("mac")) {
            command.append(DRACO_ENCODER_MAC);
        }
        command.append("-i ").append(obj.getAbsolutePath()).append(" ").append("-o ").append(drc.getAbsolutePath());
        try {
            services.run(command.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        obj.delete();
        return drc;
    }

    @Override
    public void deleteModel(String id) throws Exception {
        Optional<Geometry> model = modelRepository.findById(id);
        if (model.isPresent()) {
            Project project = model.get().getProject();
            List<Geometry> list = project.getModels();
            list.remove(model.get());
            project.setModels(list);
            projectRepository.save(project);

            String path = model.get().getInputLink().get("stl");
            deleteObjectsFromS3(path);

            modelRepository.deleteById(id);
        } else {
            throw new Exception("Model not found");
        }
    }

    @Override
    public URL getDownloadLinkFromS3(String path) {
        Date expiration = new java.util.Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 60;
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(S3_BUCKET_NAME, path)
            .withMethod(HttpMethod.GET)
            .withExpiration(expiration);
        return s3client.generatePresignedUrl(generatePresignedUrlRequest);
    }

    @Override
    public ResponseEntity getFileContent(String projectId, String modelId, String format) throws Exception {
        System.out.println("in get file content");

        Optional<User> user = userRepository.findOneByLogin(userService.getUserWithAuthorities().get().getLogin());
        if (user.isPresent()) {
            Optional<Project> project = projectRepository.findById(projectId);
            if (project.isPresent()) {
                Optional<Geometry> model = modelRepository.findById(modelId);
                if (model.isPresent()) {
                    if (format.equals("stl")) {
                        String path = model.get().getInputLink().get("stl");
                        boolean exists = s3client.doesObjectExist(S3_BUCKET_NAME, path);
                        if (exists) {
                            return ResponseEntity.ok().body(getDownloadLinkFromS3(path));
                        } else {
                            return ResponseEntity.notFound().build();
                        }
                    } else if (format.equals("drc")) {
                        String path = model.get().getInputLink().get("drc");
                        boolean exists = s3client.doesObjectExist(S3_BUCKET_NAME, path);
                        if (exists) {
                            return ResponseEntity.ok().body(getDownloadLinkFromS3(path));
                        } else {
                            return ResponseEntity.notFound().build();
                        }
                    } else {
                        return ResponseEntity.badRequest().body("Unsupported format");
                    }
                } else {
                    throw new Exception("Model not found");
                }
            } else {
                throw new Exception("Project not found");
            }
        } else {
            throw new Exception("User Not Found");
        }
    }

    @Override
    public List<Geometry> getAllModelsByProject(String projectId) throws Exception {
        Optional<User> user = userRepository.findOneByLogin(userService.getUserWithAuthorities().get().getLogin());
        if (user.isPresent()) {
            Optional<Project> project = projectRepository.findById(projectId);
            if (project.isPresent()) {
                ObjectId id = new ObjectId(projectId);
                return modelRepository.getAllModelsByProject(id);
            } else {
                throw new Exception("Project not found");
            }
        } else {
            throw new Exception("User not found");
        }
    }

    @Override
    public List<Geometry> getAllModelsByUser() throws Exception {
        Optional<User> user = userRepository.findOneByLogin(userService.getUserWithAuthorities().get().getLogin());
        if (user.isPresent()) {
            ObjectId userId = new ObjectId(user.get().getId());
            return modelRepository.getAllModelsByUser(userId);
        } else {
            throw new Exception("User not found");
        }
    }

    @Override
    public void deleteObjectsFromS3(String path) {
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(S3_BUCKET_NAME).withPrefix(path);
        ObjectListing objectListing = s3client.listObjects(listObjectsRequest);
        while (true) {
            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                s3client.deleteObject(S3_BUCKET_NAME, objectSummary.getKey());
            }
            if (objectListing.isTruncated()) {
                objectListing = s3client.listNextBatchOfObjects(objectListing);
            } else {
                break;
            }
        }
    }

    public ResponseEntity deleteGeometriesByProjectId(ObjectId projectId) {
        try {
            modelRepository.deleteGeometriesByProjectId(projectId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
