package com.esatic.assignmentapp.repository;

import com.esatic.assignmentapp.model.Class;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ClassRepository extends MongoRepository<Class, String> {
}