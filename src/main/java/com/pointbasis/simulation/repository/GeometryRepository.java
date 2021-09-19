package com.pointbasis.simulation.repository;

import com.pointbasis.simulation.domain.Geometry;
import java.util.List;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the Geometry entity.
 */
@SuppressWarnings("unused")
@Repository
public interface GeometryRepository extends MongoRepository<Geometry, String> {
    @Query("{'user.$id': ?0 }")
    List<Geometry> getAllModelsByUser(ObjectId userId);

    @Query("{'project.$id': ?0 }")
    List<Geometry> getAllModelsByProject(ObjectId projectId);

    @Query(value = "{'project.$id': ?0 }", delete = true)
    void deleteGeometriesByProjectId(Object projectId);
}
