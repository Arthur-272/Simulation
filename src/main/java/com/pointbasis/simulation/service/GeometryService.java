package com.pointbasis.simulation.service;

import com.pointbasis.simulation.domain.Geometry;
import java.io.File;
import java.net.URL;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface GeometryService {
    ResponseEntity<Geometry> createModel(MultipartFile object, String projectId) throws Exception;

    List<Geometry> getAllModelsByUser() throws Exception;

    List<Geometry> getAllModelsByProject(String projectId) throws Exception;

    void deleteModel(String id) throws Exception;

    URL getDownloadLinkFromS3(String path);

    void deleteObjectsFromS3(String path);

    File convertToDRC(File file);

    ResponseEntity getFileContent(String projectId, String modelId, String format) throws Exception;
}
