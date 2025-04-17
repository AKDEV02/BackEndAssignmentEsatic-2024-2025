package com.esatic.assignmentapp.controller;

import com.esatic.assignmentapp.dto.AssignmentDTO;
import com.esatic.assignmentapp.dto.ErrorResponse;
import com.esatic.assignmentapp.dto.PaginatedResponse;
import com.esatic.assignmentapp.exception.ResourceNotFoundException;
import com.esatic.assignmentapp.model.Assignment;
import com.esatic.assignmentapp.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
@Slf4j
public class AssignmentController {

    private final AssignmentService assignmentService;

    @GetMapping
    public ResponseEntity<?> getAllAssignments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            return ResponseEntity.ok(assignmentService.getAllAssignments(page, limit));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des assignments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors de la récupération des assignments", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAssignmentById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(assignmentService.getAssignmentById(id));
        } catch (ResourceNotFoundException e) {
            log.error("Assignment non trouvé avec l'ID: " + id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Assignment non trouvé", e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'assignment: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors de la récupération de l'assignment", e.getMessage()));
        }
    }

    @PostMapping
    // Annotation de sécurité supprimée
    public ResponseEntity<?> createAssignment(@Valid @RequestBody AssignmentDTO assignmentDTO) {
        try {
            log.info("Tentative de création d'un assignment: {}", assignmentDTO);
            Assignment created = assignmentService.createAssignment(assignmentDTO);
            log.info("Assignment créé avec succès: {}", created);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            log.error("Ressource non trouvée lors de la création de l'assignment", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Ressource non trouvée", e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur lors de la création de l'assignment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors de la création de l'assignment", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    // Annotation de sécurité supprimée
    public ResponseEntity<?> updateAssignment(
            @PathVariable String id,
            @Valid @RequestBody AssignmentDTO assignmentDTO
    ) {
        try {
            log.info("Tentative de mise à jour de l'assignment {}: {}", id, assignmentDTO);
            Assignment updated = assignmentService.updateAssignment(id, assignmentDTO);
            log.info("Assignment mis à jour avec succès: {}", updated);
            return ResponseEntity.ok(updated);
        } catch (ResourceNotFoundException e) {
            log.error("Ressource non trouvée lors de la mise à jour de l'assignment: " + id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Ressource non trouvée", e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de l'assignment: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors de la mise à jour de l'assignment", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    // Annotation de sécurité supprimée
    public ResponseEntity<?> deleteAssignment(@PathVariable String id) {
        try {
            log.info("Tentative de suppression de l'assignment: {}", id);
            assignmentService.deleteAssignment(id);
            log.info("Assignment supprimé avec succès: {}", id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ResourceNotFoundException e) {
            log.error("Assignment non trouvé lors de la suppression: " + id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Assignment non trouvé", e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de l'assignment: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors de la suppression de l'assignment", e.getMessage()));
        }
    }

    @GetMapping("/submitted")
    public ResponseEntity<?> getSubmittedAssignments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            return ResponseEntity.ok(assignmentService.getSubmittedAssignments(page, limit));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des assignments soumis", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors de la récupération des assignments soumis", e.getMessage()));
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingAssignments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            return ResponseEntity.ok(assignmentService.getPendingAssignments(page, limit));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des assignments en attente", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors de la récupération des assignments en attente", e.getMessage()));
        }
    }

    @GetMapping("/subject/{subjectId}")
    public ResponseEntity<?> getAssignmentsBySubject(
            @PathVariable String subjectId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            return ResponseEntity.ok(assignmentService.getAssignmentsBySubject(subjectId, page, limit));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des assignments par matière: " + subjectId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors de la récupération des assignments par matière", e.getMessage()));
        }
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getAssignmentsByStudent(
            @PathVariable String studentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            return ResponseEntity.ok(assignmentService.getAssignmentsByStudent(studentId, page, limit));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des assignments par étudiant: " + studentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors de la récupération des assignments par étudiant", e.getMessage()));
        }
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<?> getAssignmentsByTeacher(
            @PathVariable String teacherId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            return ResponseEntity.ok(assignmentService.getAssignmentsByTeacher(teacherId, page, limit));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des assignments par professeur: " + teacherId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors de la récupération des assignments", e.getMessage()));
        }
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<?> getAssignmentsByClass(
            @PathVariable String classId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            return ResponseEntity.ok(assignmentService.getAssignmentsByClass(classId, page, limit));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des assignments par classe: " + classId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors de la récupération des assignments", e.getMessage()));
        }
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> submitAssignment(
            @PathVariable String id,
            @RequestBody Map<String, Object> payload
    ) {
        try {
            String studentId = (String) payload.get("studentId");
            @SuppressWarnings("unchecked")
            List<String> attachments = (List<String>) payload.get("attachments");

            Assignment submitted = assignmentService.submitAssignment(id, studentId, attachments);
            return ResponseEntity.ok(submitted);
        } catch (Exception e) {
            log.error("Erreur lors de la soumission de l'assignment: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors de la soumission de l'assignment", e.getMessage()));
        }
    }

    @PostMapping("/{id}/grade")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> gradeAssignment(
            @PathVariable String id,
            @RequestBody Map<String, Object> payload
    ) {
        try {
            Double note = Double.valueOf(payload.get("note").toString());
            String remarques = (String) payload.get("remarques");

            Assignment graded = assignmentService.gradeAssignment(id, note, remarques);
            return ResponseEntity.ok(graded);
        } catch (Exception e) {
            log.error("Erreur lors de la notation de l'assignment: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors de la notation de l'assignment", e.getMessage()));
        }
    }
}