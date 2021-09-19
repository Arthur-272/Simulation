package com.pointbasis.simulation.service.impl;

import static com.pointbasis.simulation.others.Constants.*;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.pointbasis.simulation.domain.Geometry;
import com.pointbasis.simulation.domain.Mesh;
import com.pointbasis.simulation.domain.Project;
import com.pointbasis.simulation.domain.User;
import com.pointbasis.simulation.repository.GeometryRepository;
import com.pointbasis.simulation.repository.MeshRepository;
import com.pointbasis.simulation.repository.ProjectRepository;
import com.pointbasis.simulation.repository.UserRepository;
import com.pointbasis.simulation.service.MeshService;
import com.pointbasis.simulation.service.Services;
import com.pointbasis.simulation.service.UserService;
import java.net.URL;
import java.util.*;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class MeshServiceImpl implements MeshService {

    private final Logger log = LoggerFactory.getLogger(MeshServiceImpl.class);

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
    private MeshRepository meshRepository;

    @Autowired
    private Services services;

    @Override
    public String downloadObject(String projectId, String meshId, String format) throws Exception {
        String link = null;
        Optional<User> user = userRepository.findOneByLogin(userService.getUserWithAuthorities().get().getLogin());
        if (user.isPresent()) {
            Optional<Project> project = projectRepository.findById(projectId);
            if (project.isPresent()) {
                Optional<Mesh> mesh = meshRepository.findById(meshId);
                if (mesh.isPresent()) {
                    //                    String command = "ssh -i " + CLUSTERKEY + " " + services.getIP() + " bash ~/HeadScripts/uploadObject.sh";
                    //                    services.run(command);
                    Map<String, String> outputLink = mesh.get().getOutputLink();
                    if (format.equals("msh")) {
                        link = getDownloadLinkFromS3(outputLink.get("msh")).toString();
                    } else if (format.equals("obj")) {
                        link = getDownloadLinkFromS3(outputLink.get("obj")).toString();
                    }
                    System.out.println(link);
                    return link;
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
    public Mesh createMesh(Geometry model, String edgeLength, String tolerance) throws Exception {
        String userId = model.getProject().getUser().getId();
        String projectId = model.getProject().getId();
        String modelName = model.getName();
        Map<String, String> outputLink = new HashMap<>();
        String msh = userId + "/" + projectId + "/outputFiles/" + modelName + ".msh";
        String obj = userId + "/" + projectId + "/outputFiles/" + modelName + ".msh__sf.obj";
        String drc = userId + "/" + projectId + "/outputFiles/" + modelName + ".drc";
        outputLink.put("msh", msh);
        outputLink.put("obj", obj);
        outputLink.put("drc", drc);

        ObjectId meshId = new ObjectId();
        Mesh mesh = new Mesh();
        mesh.setId(meshId.toHexString());
        mesh.setHasBeenSimulated(false);
        mesh.setModel(model);
        mesh.setOutputLink(outputLink);
        mesh.setEdgeLength(Double.parseDouble(edgeLength));
        mesh.setTolerance(Double.parseDouble(tolerance));
        mesh.setProject(model.getProject());
        meshRepository.save(mesh);
        return mesh;
    }

    @Override
    public ResponseEntity getFileContent(String projectId, String meshId, String format) throws Exception {
        System.out.println("in get file content");

        Optional<User> user = userRepository.findOneByLogin(userService.getUserWithAuthorities().get().getLogin());
        if (user.isPresent()) {
            Optional<Project> project = projectRepository.findById(projectId);
            if (project.isPresent()) {
                Optional<Mesh> mesh = meshRepository.findById(meshId);
                if (mesh.isPresent()) {
                    if (format.equals("obj")) {
                        String path = mesh.get().getOutputLink().get("obj");
                        boolean exists = s3client.doesObjectExist(S3_BUCKET_NAME, path);
                        if (exists) {
                            return ResponseEntity.ok().body(getDownloadLinkFromS3(path));
                        } else {
                            return ResponseEntity.notFound().build();
                        }
                    } else if (format.equals("drc")) {
                        String path = mesh.get().getOutputLink().get("drc");
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
                    throw new Exception("Mesh not found");
                }
            } else {
                throw new Exception("Project not found");
            }
        } else {
            throw new Exception("User Not Found");
        }
    }

    @Override
    public void deleteMesh(String id, String ip) throws Exception {
        Optional<Mesh> mesh = meshRepository.findById(id);
        if (mesh.isPresent()) {
            String path = mesh.get().getOutputLink().get("msh");
            deleteObjectsFromS3(path);

            path = mesh.get().getOutputLink().get("obj");
            deleteObjectsFromS3(path);

            StringBuilder command = new StringBuilder();
            command
                .append("ssh -i ")
                .append(CLUSTERKEY)
                .append(" ")
                .append(ip)
                .append(" sudo rm -r /shared/")
                .append(mesh.get().getProject().getUser().getId())
                .append("/")
                .append(mesh.get().getProject().getId())
                .append("/")
                .append(mesh.get().getModel().getId())
                .append("/");
            services.run(command.toString());

            Project project = mesh.get().getProject();
            List<Mesh> list = project.getMeshes();
            list.remove(mesh.get());
            project.setMeshes(list);
            projectRepository.save(project);

            meshRepository.deleteById(id);
        } else {
            throw new Exception("Model not found");
        }
    }

    @Override
    public List<Mesh> getAllMeshesByProject(String projectId) throws Exception {
        Optional<User> user = userRepository.findOneByLogin(userService.getUserWithAuthorities().get().getLogin());
        if (user.isPresent()) {
            Optional<Project> project = projectRepository.findById(projectId);
            if (project.isPresent()) {
                ObjectId id = new ObjectId(projectId);
                return meshRepository.getAllMeshesByProject(id);
            } else {
                throw new Exception("Project not found");
            }
        } else {
            throw new Exception("User not found");
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

    public ResponseEntity deleteMeshesByProjectId(ObjectId projectId) {
        try {
            meshRepository.deleteMeshesByProjectId(projectId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    public Optional<Mesh> getMeshByGeometry(String geometryId) {
        return meshRepository.getMeshByGeometry(new ObjectId(geometryId));
    }
}
