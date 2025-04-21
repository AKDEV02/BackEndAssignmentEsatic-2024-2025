package com.esatic.assignmentapp.controller;

import com.esatic.assignmentapp.dto.*;
import com.esatic.assignmentapp.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
@Slf4j
public class AssignmentController {

    private final AssignmentService assignmentService;

    @GetMapping
    public ResponseEntity<PaginatedResponse<AssignmentResponseDTO>> getAllAssignments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(assignmentService.getAllAssignments(page, limit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssignmentResponseDTO> getAssignmentById(@PathVariable String id) {
        return ResponseEntity.ok(assignmentService.getAssignmentById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<AssignmentResponseDTO> createAssignment(
            @Valid @RequestBody AssignmentCreateDTO assignmentDTO) {
        AssignmentResponseDTO created = assignmentService.createAssignment(assignmentDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<AssignmentResponseDTO> updateAssignment(
            @PathVariable String id,
            @Valid @RequestBody AssignmentUpdateDTO assignmentDTO) {
        return ResponseEntity.ok(assignmentService.updateAssignment(id, assignmentDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Void> deleteAssignment(@PathVariable String id) {
        assignmentService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/submitted")
    public ResponseEntity<PaginatedResponse<AssignmentResponseDTO>> getSubmittedAssignments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(assignmentService.getSubmittedAssignments(page, limit));
    }

    @GetMapping("/pending")
    public ResponseEntity<PaginatedResponse<AssignmentResponseDTO>> getPendingAssignments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(assignmentService.getPendingAssignments(page, limit));
    }

    @GetMapping("/subject/{subjectId}")
    public ResponseEntity<PaginatedResponse<AssignmentResponseDTO>> getAssignmentsBySubject(
            @PathVariable String subjectId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(assignmentService.getAssignmentsBySubject(subjectId, page, limit));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<PaginatedResponse<AssignmentResponseDTO>> getAssignmentsByStudent(
            @PathVariable String studentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByStudent(studentId, page, limit));
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<PaginatedResponse<AssignmentResponseDTO>> getAssignmentsByTeacher(
            @PathVariable String teacherId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByTeacher(teacherId, page, limit));
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<PaginatedResponse<AssignmentResponseDTO>> getAssignmentsByClass(
            @PathVariable String classId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByClass(classId, page, limit));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AssignmentResponseDTO> submitAssignment(
            @PathVariable String id,
            @Valid @RequestBody AssignmentSubmissionDTO submitDTO) {
        return ResponseEntity.ok(assignmentService.submitAssignment(id, submitDTO));
    }

    @PostMapping("/{id}/grade")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<AssignmentResponseDTO> gradeAssignment(
            @PathVariable String id,
            @Valid @RequestBody AssignmentGradingDTO gradeDTO) {
        return ResponseEntity.ok(assignmentService.gradeAssignment(id, gradeDTO));
    }
}