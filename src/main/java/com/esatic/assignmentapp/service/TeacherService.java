package com.esatic.assignmentapp.service;

import com.esatic.assignmentapp.dto.SubjectInfoDTO;
import com.esatic.assignmentapp.dto.TeacherDTO;
import com.esatic.assignmentapp.dto.TeacherResponseDTO;
import com.esatic.assignmentapp.exception.ResourceNotFoundException;
import com.esatic.assignmentapp.model.Teacher;
import com.esatic.assignmentapp.repository.TeacherRepository;
import com.esatic.assignmentapp.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;

    public List<TeacherResponseDTO> getAllTeachers() {
        return teacherRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TeacherResponseDTO getTeacherById(String id) {
        return teacherRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));
    }

    @Transactional
    public TeacherResponseDTO createTeacher(TeacherDTO teacherDTO) {
        Teacher teacher = new Teacher();
        teacher.setFirstName(teacherDTO.getFirstName());
        teacher.setLastName(teacherDTO.getLastName());
        teacher.setEmail(teacherDTO.getEmail());
        teacher.setPhotoUrl(teacherDTO.getPhotoUrl());
        teacher.setSubjects(teacherDTO.getSubjectIds());
        teacher.setCreatedAt(new Date());
        teacher.setUpdatedAt(new Date());

        return convertToDTO(teacherRepository.save(teacher));
    }

    @Transactional
    public TeacherResponseDTO updateTeacher(String id, TeacherDTO teacherDTO) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));

        teacher.setFirstName(teacherDTO.getFirstName());
        teacher.setLastName(teacherDTO.getLastName());
        teacher.setEmail(teacherDTO.getEmail());
        teacher.setPhotoUrl(teacherDTO.getPhotoUrl());
        teacher.setSubjects(teacherDTO.getSubjectIds());
        teacher.setUpdatedAt(new Date());

        return convertToDTO(teacherRepository.save(teacher));
    }

    @Transactional
    public void deleteTeacher(String id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));
        teacherRepository.delete(teacher);
    }

    private TeacherResponseDTO convertToDTO(Teacher teacher) {
        return TeacherResponseDTO.builder()
                .id(teacher.getId())
                .firstName(teacher.getFirstName())
                .lastName(teacher.getLastName())
                .email(teacher.getEmail())
                .photoUrl(teacher.getPhotoUrl())
                .subjects(subjectRepository.findAllById(teacher.getSubjects()).stream()
                        .map(subject -> new SubjectInfoDTO(
                                subject.getId(),
                                subject.getName(),
                                subject.getImageUrl(),
                                subject.getColor()))
                        .collect(Collectors.toList()))
                .createdAt(teacher.getCreatedAt())
                .updatedAt(teacher.getUpdatedAt())
                .build();
    }
}