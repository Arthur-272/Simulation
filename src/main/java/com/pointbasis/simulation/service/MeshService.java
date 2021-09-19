package com.pointbasis.simulation.service;

import com.pointbasis.simulation.domain.Geometry;
import com.pointbasis.simulation.domain.Mesh;
import java.net.URL;
import java.util.List;
import org.springframework.http.ResponseEntity;

public interface MeshService {
    String downloadObject(String projectId, String modelId, String format) throws Exception;

    URL getDownloadLinkFromS3(String path);

    void deleteObjectsFromS3(String path);

    void deleteMesh(String id, String ip) throws Exception;

    Mesh createMesh(Geometry model, String edgeLength, String tolerance) throws Exception;

    List<Mesh> getAllMeshesByProject(String projectId) throws Exception;

    ResponseEntity getFileContent(String projectId, String meshId, String format) throws Exception;
}
