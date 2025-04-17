package com.esatic.assignmentapp.service;

import com.esatic.assignmentapp.exception.ResourceNotFoundException;
import com.esatic.assignmentapp.model.Class;
import com.esatic.assignmentapp.model.User;
import com.esatic.assignmentapp.repository.ClassRepository;
import com.esatic.assignmentapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassService {

    private final ClassRepository classRepository;
    private final UserRepository userRepository;

    public List<Class> getAllClasses() {
        return classRepository.findAll();
    }

    public Class getClassById(String id) {
        return classRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classe non trouvée avec l'ID: " + id));
    }

    public Class createClass(Class classData) {
        classData.setCreatedAt(new Date());
        classData.setUpdatedAt(new Date());
        return classRepository.save(classData);
    }

    public Class updateClass(String id, Class classData) {
        Class existingClass = getClassById(id);

        existingClass.setName(classData.getName());
        existingClass.setYear(classData.getYear());
        existingClass.setDescription(classData.getDescription());
        existingClass.setUpdatedAt(new Date());

        return classRepository.save(existingClass);
    }

    public void deleteClass(String id) {
        Class classToDelete = getClassById(id);

        // Suppression des références dans les utilisateurs
        if (classToDelete.getStudents() != null) {
            for (User student : classToDelete.getStudents()) {
                student.setClassId(null);
                userRepository.save(student);
            }
        }

        classRepository.delete(classToDelete);
    }

    public List<User> getStudentsByClassId(String classId) {
        Class classEntity = getClassById(classId);
        return classEntity.getStudents();
    }

    public Class addStudentToClass(String classId, String studentId) {
        Class classEntity = getClassById(classId);
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé avec l'ID: " + studentId));

        if (!"STUDENT".equals(student.getRole())) {
            throw new IllegalArgumentException("L'utilisateur n'est pas un étudiant");
        }

        // Ajouter l'étudiant à la classe s'il n'y est pas déjà
        if (classEntity.getStudents() == null || !classEntity.getStudents().contains(student)) {
            classEntity.getStudents().add(student);
        }

        // Attribuer la classe à l'étudiant
        student.setClassId(classEntity);
        userRepository.save(student);

        return classRepository.save(classEntity);
    }

    public Class removeStudentFromClass(String classId, String studentId) {
        Class classEntity = getClassById(classId);
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé avec l'ID: " + studentId));

        // Retirer l'étudiant de la classe
        classEntity.getStudents().removeIf(s -> s.getId().equals(studentId));

        // Retirer la référence de la classe de l'étudiant
        if (student.getClassId() != null && student.getClassId().getId().equals(classId)) {
            student.setClassId(null);
            userRepository.save(student);
        }

        return classRepository.save(classEntity);
    }
}