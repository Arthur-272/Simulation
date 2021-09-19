package com.pointbasis.simulation.repository;

import com.pointbasis.simulation.domain.Geometry;
import com.pointbasis.simulation.domain.Mesh;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the Mesh entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MeshRepository extends MongoRepository<Mesh, String> {
    @Query("{'user.$id': ?0 }")
    List<Mesh> getAllMeshesByUser(ObjectId userId);

    @Query("{'project.$id': ?0, 'simulated': true }")
    List<Mesh> getAllMeshesByProject(ObjectId modelId);

    @Query(value = "{'project.$id': ?0 }", delete = true)
    void deleteMeshesByProjectId(ObjectId projectId);

    @Query(value = "{'model.$id': ?0 }")
    Optional<Mesh> getMeshByGeometry(ObjectId modelId);
}
