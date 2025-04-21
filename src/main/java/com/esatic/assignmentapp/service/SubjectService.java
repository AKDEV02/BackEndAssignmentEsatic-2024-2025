package com.esatic.assignmentapp.service;

import com.esatic.assignmentapp.dto.SubjectDTO;
import com.esatic.assignmentapp.dto.SubjectResponseDTO;
import com.esatic.assignmentapp.exception.ResourceNotFoundException;
import com.esatic.assignmentapp.model.Subject;
import com.esatic.assignmentapp.model.Teacher;
import com.esatic.assignmentapp.repository.SubjectRepository;
import com.esatic.assignmentapp.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;

    public List<SubjectResponseDTO> getAllSubjects() {
        return subjectRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public SubjectResponseDTO getSubjectById(String id) {
        return subjectRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", id));
    }

    private SubjectResponseDTO convertToDTO(Subject subject) {
        return SubjectResponseDTO.builder()
                .id(subject.getId())
                .name(subject.getName())
                .imageUrl(subject.getImageUrl())
                .teacherId(subject.getTeacher() != null ? subject.getTeacher().getId() : null)
                .teacherName(subject.getTeacher() != null ?
                        subject.getTeacher().getFirstName() + " " + subject.getTeacher().getLastName() : null)
                .color(subject.getColor())
                .description(subject.getDescription())
                .createdAt(subject.getCreatedAt())
                .updatedAt(subject.getUpdatedAt())
                .build();
    }

    public SubjectResponseDTO createSubject(SubjectDTO subjectDTO) {
        Subject subject = new Subject();
        subject.setName(subjectDTO.getName());
        subject.setImageUrl(subjectDTO.getImageUrl());
        subject.setColor(subjectDTO.getColor());
        subject.setDescription(subjectDTO.getDescription());
        subject.setCreatedAt(new Date());
        subject.setUpdatedAt(new Date());

        if (subjectDTO.getTeacherId() != null) {
            teacherRepository.findById(subjectDTO.getTeacherId())
                    .ifPresent(subject::setTeacher);
        }

        return convertToDTO(subjectRepository.save(subject));
    }

    public SubjectResponseDTO updateSubject(String id, SubjectDTO subjectDTO) {
        Subject subject = getSubjectEntityById(id);

        subject.setName(subjectDTO.getName());
        subject.setImageUrl(subjectDTO.getImageUrl());
        subject.setColor(subjectDTO.getColor());
        subject.setDescription(subjectDTO.getDescription());
        subject.setUpdatedAt(new Date());

        if (subjectDTO.getTeacherId() != null) {
            if (subjectDTO.getTeacherId().trim().isEmpty()) {
                subject.setTeacher(null);
            } else {
                teacherRepository.findById(subjectDTO.getTeacherId())
                        .ifPresentOrElse(
                                subject::setTeacher,
                                () -> subject.setTeacher(null)
                        );
            }
        }

        return convertToDTO(subjectRepository.save(subject));
    }

    private Subject getSubjectEntityById(String id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", id));
    }

    public void deleteSubject(String id) {
        Subject subject = getSubjectEntityById(id);
        subjectRepository.delete(subject);
    }
}