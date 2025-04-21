package com.esatic.assignmentapp.service;

import com.esatic.assignmentapp.dto.ClassDTO;
import com.esatic.assignmentapp.dto.ClassResponseDTO;
import com.esatic.assignmentapp.dto.StudentInfoDTO;
import com.esatic.assignmentapp.exception.ResourceNotFoundException;
import com.esatic.assignmentapp.model.Class;
import com.esatic.assignmentapp.model.User;
import com.esatic.assignmentapp.repository.ClassRepository;
import com.esatic.assignmentapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassService {

    private final ClassRepository classRepository;
    private final UserRepository userRepository;

    // Conversion methods
    private ClassResponseDTO toResponseDTO(Class classEntity) {
        List<StudentInfoDTO> students = userRepository.findByClassId(classEntity.getId())
                .stream()
                .map(user -> com.esatic.assignmentapp.dto.StudentInfoDTO.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .build())
                .collect(Collectors.toList());

        return ClassResponseDTO.builder()
                .id(classEntity.getId())
                .name(classEntity.getName())
                .year(classEntity.getYear())
                .description(classEntity.getDescription())
                .students(students)
                .createdAt(classEntity.getCreatedAt())
                .updatedAt(classEntity.getUpdatedAt())
                .build();
    }

    private Class fromDTO(ClassDTO dto) {
        return Class.builder()
                .name(dto.getName())
                .year(dto.getYear())
                .description(dto.getDescription())
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
    }

    // Service methods
    public List<ClassResponseDTO> getAllClasses() {
        return classRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public ClassResponseDTO getClassById(String id) {
        Class classEntity = classRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classe non trouvée avec l'ID: " + id));
        return toResponseDTO(classEntity);
    }

    @Transactional
    public ClassResponseDTO createClass(ClassDTO classDTO) {
        Class newClass = fromDTO(classDTO);
        Class saved = classRepository.save(newClass);
        return toResponseDTO(saved);
    }

    @Transactional
    public ClassResponseDTO updateClass(String id, ClassDTO classDTO) {
        Class existingClass = classRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classe non trouvée avec l'ID: " + id));

        existingClass.setName(classDTO.getName());
        existingClass.setYear(classDTO.getYear());
        existingClass.setDescription(classDTO.getDescription());
        existingClass.setUpdatedAt(new Date());

        return toResponseDTO(classRepository.save(existingClass));
    }

    @Transactional
    public void deleteClass(String id) {
        Class classToDelete = classRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classe non trouvée avec l'ID: " + id));

        // Nettoyage des références
        userRepository.findByClassId(id).forEach(user -> {
            user.setClassId(null);
            userRepository.save(user);
        });

        classRepository.delete(classToDelete);
    }

    @Transactional
    public ClassResponseDTO addStudentToClass(String classId, String studentId) {
        Class classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Classe non trouvée avec l'ID: " + classId));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé avec l'ID: " + studentId));

        if (!"STUDENT".equals(student.getRole())) {
            throw new IllegalArgumentException("L'utilisateur n'est pas un étudiant");
        }

        student.setClassId(classEntity);
        userRepository.save(student);

        return toResponseDTO(classEntity);
    }

    @Transactional
    public ClassResponseDTO removeStudentFromClass(String classId, String studentId) {
        Class classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Classe non trouvée avec l'ID: " + classId));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé avec l'ID: " + studentId));

        if (student.getClassId() != null && student.getClassId().getId().equals(classId)) {
            student.setClassId(null);
            userRepository.save(student);
        }

        return toResponseDTO(classEntity);
    }
}