package com.esatic.assignmentapp.controller;

import com.esatic.assignmentapp.dto.ErrorResponse;
import com.esatic.assignmentapp.dto.SubjectDTO;
import com.esatic.assignmentapp.exception.ResourceNotFoundException;
import com.esatic.assignmentapp.model.Subject;
import com.esatic.assignmentapp.service.SubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
@Slf4j
public class SubjectController {

    private final SubjectService subjectService;

    @GetMapping
    public ResponseEntity<?> getAllSubjects() {
        try {
            return ResponseEntity.ok(subjectService.getAllSubjects());
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des matières", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors de la récupération des matières", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSubjectById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(subjectService.getSubjectById(id));
        } catch (ResourceNotFoundException e) {
            log.error("Matière non trouvée avec l'ID: " + id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Matière non trouvée", e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la matière: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors de la récupération de la matière", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createSubject(@Valid @RequestBody SubjectDTO subjectDTO) {
        try {
            log.info("Tentative de création d'une matière: {}", subjectDTO);
            Subject created = subjectService.createSubject(subjectDTO);
            log.info("Matière créée avec succès: {}", created);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            log.error("Ressource non trouvée lors de la création de la matière", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Ressource non trouvée", e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur lors de la création de la matière", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors de la création de la matière", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSubject(
            @PathVariable String id,
            @Valid @RequestBody SubjectDTO subjectDTO
    ) {
        try {
            log.info("Tentative de mise à jour de la matière {}: {}", id, subjectDTO);
            Subject updated = subjectService.updateSubject(id, subjectDTO);
            log.info("Matière mise à jour avec succès: {}", updated);
            return ResponseEntity.ok(updated);
        } catch (ResourceNotFoundException e) {
            log.error("Ressource non trouvée lors de la mise à jour de la matière: " + id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Ressource non trouvée", e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de la matière: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors de la mise à jour de la matière", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSubject(@PathVariable String id) {
        try {
            log.info("Tentative de suppression de la matière: {}", id);
            subjectService.deleteSubject(id);
            log.info("Matière supprimée avec succès: {}", id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ResourceNotFoundException e) {
            log.error("Matière non trouvée lors de la suppression: " + id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Matière non trouvée", e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de la matière: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors de la suppression de la matière", e.getMessage()));
        }
    }
}