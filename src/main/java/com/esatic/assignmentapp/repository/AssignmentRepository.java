package com.esatic.assignmentapp.repository;

import com.esatic.assignmentapp.model.Assignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends MongoRepository<Assignment, String> {
    Page<Assignment> findByRendu(boolean rendu, Pageable pageable);
    Page<Assignment> findByMatiereId(String matiereId, Pageable pageable);
    Page<Assignment> findByAuteurId(String auteurId, Pageable pageable);

    // Additional methods needed based on the service class
    Page<Assignment> findByMatiereIdIn(List<String> matiereIds, Pageable pageable);
    Page<Assignment> findByClassId(String classId, Pageable pageable);
}