package com.pointbasis.simulation.repository;

import com.pointbasis.simulation.domain.Job;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the Job entity.
 */
@SuppressWarnings("unused")
@Repository
public interface JobRepository extends MongoRepository<Job, String> {
    @Query("{'project.$id': ?0 }")
    List<Job> getAllJobsByProject(ObjectId projectId);

    void deleteByJobId(String jobId);

    @Query("{'jobId': ?0 }")
    Optional<Job> findByJobId(String jobId);
}
