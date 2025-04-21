package com.esatic.assignmentapp.service;

import com.esatic.assignmentapp.dto.*;
import com.esatic.assignmentapp.exception.ResourceNotFoundException;
import com.esatic.assignmentapp.model.*;
import com.esatic.assignmentapp.repository.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final ClassRepository classRepository;

    // Conversion methods
    private AssignmentResponseDTO toResponseDTO(Assignment assignment) {
        return AssignmentResponseDTO.builder()
                .id(assignment.getId())
                .nom(assignment.getNom())
                .dateDeRendu(assignment.getDateDeRendu())
                .rendu(assignment.isRendu())
                .auteurId(assignment.getAuteur() != null ? assignment.getAuteur().getId() : null)
                .auteurName(assignment.getAuteur() != null ?
                        assignment.getAuteur().getFirstName() + " " + assignment.getAuteur().getLastName() : null)
                .matiereId(assignment.getMatiere() != null ? assignment.getMatiere().getId() : null)
                .matiereName(assignment.getMatiere() != null ? assignment.getMatiere().getName() : null)
                .note(assignment.getNote())
                .remarques(assignment.getRemarques())
                .classId(assignment.getClassId() != null ? assignment.getClassId().getId() : null)
                .className(assignment.getClassId() != null ? assignment.getClassId().getName() : null)
                .attachments(assignment.getAttachments())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .build();
    }

    private Assignment fromCreateDTO(AssignmentCreateDTO dto) {
        Assignment assignment = new Assignment();
        assignment.setNom(dto.getNom());
        assignment.setDateDeRendu(dto.getDateDeRendu());
        assignment.setRendu(dto.isRendu());
        assignment.setNote(dto.getNote());
        assignment.setRemarques(dto.getRemarques());
        assignment.setAttachments(dto.getAttachments());

        if (dto.getAuteurId() != null) {
            userRepository.findById(dto.getAuteurId()).ifPresent(assignment::setAuteur);
        }

        if (dto.getMatiereId() != null) {
            subjectRepository.findById(dto.getMatiereId()).ifPresent(assignment::setMatiere);
        }

        if (dto.getClassId() != null) {
            classRepository.findById(dto.getClassId()).ifPresent(assignment::setClassId);
        }

        return assignment;
    }

    // Core service methods
    public PaginatedResponse<AssignmentResponseDTO> getAllAssignments(int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "dateDeRendu"));
        return toPaginatedResponse(assignmentRepository.findAll(pageable));
    }

    public AssignmentResponseDTO getAssignmentById(String id) {
        return assignmentRepository.findById(id)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", "id", id));
    }

    @Transactional
    public AssignmentResponseDTO createAssignment(AssignmentCreateDTO assignmentDTO) {
        Assignment assignment = fromCreateDTO(assignmentDTO);
        assignment.setCreatedAt(new Date());
        assignment.setUpdatedAt(new Date());
        Assignment saved = assignmentRepository.save(assignment);
        return toResponseDTO(saved);
    }

    @Transactional
    public AssignmentResponseDTO updateAssignment(String id, AssignmentUpdateDTO assignmentDTO) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", "id", id));

        assignment.setNom(assignmentDTO.getNom());
        assignment.setDateDeRendu(assignmentDTO.getDateDeRendu());
        assignment.setRendu(assignmentDTO.isRendu());
        assignment.setNote(assignmentDTO.getNote());
        assignment.setRemarques(assignmentDTO.getRemarques());
        assignment.setUpdatedAt(new Date());

        if (assignmentDTO.getAuteurId() != null) {
            userRepository.findById(assignmentDTO.getAuteurId()).ifPresent(assignment::setAuteur);
        } else {
            assignment.setAuteur(null);
        }

        if (assignmentDTO.getMatiereId() != null) {
            subjectRepository.findById(assignmentDTO.getMatiereId()).ifPresent(assignment::setMatiere);
        } else {
            assignment.setMatiere(null);
        }

        if (assignmentDTO.getClassId() != null) {
            classRepository.findById(assignmentDTO.getClassId()).ifPresent(assignment::setClassId);
        } else {
            assignment.setClassId(null);
        }

        return toResponseDTO(assignmentRepository.save(assignment));
    }

    @Transactional
    public void deleteAssignment(String id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", "id", id));
        assignmentRepository.delete(assignment);
    }

    // Filter methods
    public PaginatedResponse<AssignmentResponseDTO> getSubmittedAssignments(int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "dateDeRendu"));
        return toPaginatedResponse(assignmentRepository.findByRendu(true, pageable));
    }

    public PaginatedResponse<AssignmentResponseDTO> getPendingAssignments(int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.ASC, "dateDeRendu"));
        return toPaginatedResponse(assignmentRepository.findByRendu(false, pageable));
    }

    public PaginatedResponse<AssignmentResponseDTO> getAssignmentsBySubject(String subjectId, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "dateDeRendu"));
        return toPaginatedResponse(assignmentRepository.findByMatiereId(subjectId, pageable));
    }

    public PaginatedResponse<AssignmentResponseDTO> getAssignmentsByStudent(String studentId, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "dateDeRendu"));
        return toPaginatedResponse(assignmentRepository.findByAuteurId(studentId, pageable));
    }

    public PaginatedResponse<AssignmentResponseDTO> getAssignmentsByTeacher(String teacherId, int page, int limit) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", teacherId));

        List<String> subjectIds = teacher.getTeachingSubjects().stream()
                .map(Subject::getId)
                .collect(Collectors.toList());

        Pageable pageable = PageRequest.of(page - 1, limit);
        return toPaginatedResponse(assignmentRepository.findByMatiereIdIn(subjectIds, pageable));
    }

    public PaginatedResponse<AssignmentResponseDTO> getAssignmentsByClass(String classId, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        return toPaginatedResponse(assignmentRepository.findByClassId(classId, pageable));
    }

    // Special operations
    @Transactional
    public AssignmentResponseDTO submitAssignment(String assignmentId, @Valid AssignmentSubmissionDTO submitDTO) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", "id", assignmentId));

        User student = userRepository.findById(submitDTO.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", submitDTO.getStudentId()));

        if (assignment.getClassId() == null || student.getClassId() == null ||
                !assignment.getClassId().getId().equals(student.getClassId().getId())) {
            throw new IllegalArgumentException("Student not authorized to submit this assignment");
        }

        assignment.setRendu(true);
        assignment.setAttachments(submitDTO.getAttachments());
        assignment.setUpdatedAt(new Date());

        return toResponseDTO(assignmentRepository.save(assignment));
    }

    @Transactional
    public AssignmentResponseDTO gradeAssignment(String assignmentId, @Valid AssignmentGradingDTO gradeDTO) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", "id", assignmentId));

        assignment.setNote(gradeDTO.getNote());
        assignment.setRemarques(gradeDTO.getRemarques());
        assignment.setUpdatedAt(new Date());

        return toResponseDTO(assignmentRepository.save(assignment));
    }

    // Helper method
    private PaginatedResponse<AssignmentResponseDTO> toPaginatedResponse(Page<Assignment> page) {
        List<AssignmentResponseDTO> dtos = page.getContent().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());

        return PaginatedResponse.<AssignmentResponseDTO>builder()
                .docs(dtos)
                .totalDocs(page.getTotalElements())
                .limit(page.getSize())
                .page(page.getNumber() + 1)
                .totalPages(page.getTotalPages())
                .hasPrevPage(page.hasPrevious())
                .hasNextPage(page.hasNext())
                .build();
    }
}