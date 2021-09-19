package com.pointbasis.simulation.repository;

import com.pointbasis.simulation.domain.Project;
import java.util.List;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the Project entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ProjectRepository extends MongoRepository<Project, String> {
    @Query("{'user.$id': ?0 }")
    List<Project> getAllProjectsByUser(ObjectId userId);
}
