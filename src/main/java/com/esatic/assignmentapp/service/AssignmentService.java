package com.esatic.assignmentapp.service;

import com.esatic.assignmentapp.dto.AssignmentDTO;
import com.esatic.assignmentapp.dto.PaginatedResponse;
import com.esatic.assignmentapp.exception.ResourceNotFoundException;
import com.esatic.assignmentapp.model.Assignment;
import com.esatic.assignmentapp.model.Class;
import com.esatic.assignmentapp.model.Subject;
import com.esatic.assignmentapp.model.User;
import com.esatic.assignmentapp.repository.AssignmentRepository;
import com.esatic.assignmentapp.repository.ClassRepository;
import com.esatic.assignmentapp.repository.SubjectRepository;
import com.esatic.assignmentapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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

    public PaginatedResponse<Assignment> getAllAssignments(int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "dateDeRendu"));
        Page<Assignment> assignmentsPage = assignmentRepository.findAll(pageable);

        return createPaginatedResponse(assignmentsPage);
    }

    public Assignment getAssignmentById(String id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", "id", id));
    }

    public PaginatedResponse<Assignment> getAssignmentsByTeacher(String teacherId, int page, int limit) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Professeur non trouvé avec l'ID: " + teacherId));

        // Récupérer les IDs des matières enseignées par le professeur
        List<String> teachingSubjectIds = teacher.getTeachingSubjects().stream()
                .map(Subject::getId)
                .collect(Collectors.toList());

        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Assignment> assignmentsPage = assignmentRepository.findByMatiereIdIn(teachingSubjectIds, pageable);

        return createPaginatedResponse(assignmentsPage);
    }

    // Pour les assignments d'une classe
    public PaginatedResponse<Assignment> getAssignmentsByClass(String classId, int page, int limit) {
        Class classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Classe non trouvée avec l'ID: " + classId));

        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Assignment> assignmentsPage = assignmentRepository.findByClassId(classId, pageable);

        return createPaginatedResponse(assignmentsPage);
    }

    // Pour qu'un étudiant soumette un devoir
    public Assignment submitAssignment(String assignmentId, String studentId, List<String> attachments) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment non trouvé avec l'ID: " + assignmentId));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé avec l'ID: " + studentId));

        // Vérifier que l'étudiant appartient à la classe assignée
        if (assignment.getClassId() == null || student.getClassId() == null ||
                !assignment.getClassId().getId().equals(student.getClassId().getId())) {
            throw new RuntimeException("Cet étudiant n'est pas autorisé à soumettre ce devoir");
        }

        // Mettre à jour le devoir
        assignment.setRendu(true);
        assignment.setAttachments(attachments);
        assignment.setUpdatedAt(new Date());

        return assignmentRepository.save(assignment);
    }

    // Pour noter un devoir
    public Assignment gradeAssignment(String assignmentId, Double note, String remarques) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment non trouvé avec l'ID: " + assignmentId));

        assignment.setNote(note);
        assignment.setRemarques(remarques);
        assignment.setUpdatedAt(new Date());

        return assignmentRepository.save(assignment);
    }


    public Assignment createAssignment(AssignmentDTO assignmentDTO) {
        log.info("Création d'un assignment avec DTO: {}", assignmentDTO);

        try {
            // Trouver l'auteur (utilisateur) si spécifié - sinon laisser null
            User auteur = null;
            if (assignmentDTO.getAuteur() != null && !assignmentDTO.getAuteur().trim().isEmpty()) {
                log.info("Recherche de l'utilisateur: {}", assignmentDTO.getAuteur());
                try {
                    auteur = userRepository.findByUsername(assignmentDTO.getAuteur()).orElse(null);
                    if (auteur == null) {
                        log.warn("Utilisateur '{}' non trouvé. L'assignment sera créé sans auteur.", assignmentDTO.getAuteur());
                    }
                } catch (Exception e) {
                    log.warn("Erreur lors de la recherche de l'utilisateur '{}'. L'assignment sera créé sans auteur: {}",
                            assignmentDTO.getAuteur(), e.getMessage());
                }
            }

            // Trouver la matière si spécifiée
            Subject matiere = null;
            if (assignmentDTO.getMatiere() != null && !assignmentDTO.getMatiere().trim().isEmpty()) {
                log.info("Recherche de la matière avec ID: {}", assignmentDTO.getMatiere());
                try {
                    matiere = subjectRepository.findById(assignmentDTO.getMatiere()).orElse(null);
                    if (matiere == null) {
                        log.warn("Matière avec ID '{}' non trouvée. L'assignment sera créé sans matière.", assignmentDTO.getMatiere());
                    } else {
                        log.info("Matière trouvée: {}", matiere);
                    }
                } catch (Exception e) {
                    log.warn("Erreur lors de la recherche de la matière '{}'. L'assignment sera créé sans matière: {}",
                            assignmentDTO.getMatiere(), e.getMessage());
                }
            }

            // Créer l'assignment - avec auteur et matière potentiellement null
            Assignment assignment = Assignment.builder()
                    .nom(assignmentDTO.getNom())
                    .dateDeRendu(assignmentDTO.getDateDeRendu())
                    .rendu(assignmentDTO.isRendu())
                    .auteur(auteur)  // Peut être null
                    .matiere(matiere)  // Peut être null
                    .note(assignmentDTO.getNote())
                    .remarques(assignmentDTO.getRemarques())
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .build();

            log.info("Sauvegarde de l'assignment: {}", assignment);
            return assignmentRepository.save(assignment);
        } catch (Exception e) {
            log.error("Erreur lors de la création de l'assignment: {}", e.getMessage(), e);
            throw e;
        }
    }

    public Assignment updateAssignment(String id, AssignmentDTO assignmentDTO) {
        log.info("Mise à jour de l'assignment {} avec DTO: {}", id, assignmentDTO);

        try {
            Assignment assignment = getAssignmentById(id);

            // Mettre à jour les propriétés
            assignment.setNom(assignmentDTO.getNom());
            assignment.setDateDeRendu(assignmentDTO.getDateDeRendu());
            assignment.setRendu(assignmentDTO.isRendu());
            assignment.setNote(assignmentDTO.getNote());
            assignment.setRemarques(assignmentDTO.getRemarques());
            assignment.setUpdatedAt(new Date());

            // Mettre à jour l'auteur si nécessaire - permettre null
            if (assignmentDTO.getAuteur() != null) {
                log.info("Mise à jour de l'auteur: {}", assignmentDTO.getAuteur());
                if (assignmentDTO.getAuteur().trim().isEmpty()) {
                    // Si une chaîne vide est fournie, définir l'auteur sur null
                    assignment.setAuteur(null);
                    log.info("Auteur défini sur null (chaîne vide fournie)");
                } else {
                    // Rechercher l'utilisateur, laisser l'auteur inchangé s'il n'est pas trouvé
                    try {
                        User auteur = userRepository.findByUsername(assignmentDTO.getAuteur()).orElse(null);
                        if (auteur == null) {
                            log.warn("Utilisateur '{}' non trouvé lors de la mise à jour. Auteur inchangé.", assignmentDTO.getAuteur());
                        } else {
                            assignment.setAuteur(auteur);
                        }
                    } catch (Exception e) {
                        log.warn("Erreur lors de la recherche de l'utilisateur '{}'. Auteur inchangé: {}",
                                assignmentDTO.getAuteur(), e.getMessage());
                    }
                }
            }

            // Mettre à jour la matière si nécessaire - permettre null
            if (assignmentDTO.getMatiere() != null) {
                log.info("Mise à jour de la matière avec ID: {}", assignmentDTO.getMatiere());
                if (assignmentDTO.getMatiere().trim().isEmpty()) {
                    // Si une chaîne vide est fournie, définir la matière sur null
                    assignment.setMatiere(null);
                    log.info("Matière définie sur null (chaîne vide fournie)");
                } else {
                    // Rechercher la matière, laisser inchangée si elle n'est pas trouvée
                    try {
                        Subject matiere = subjectRepository.findById(assignmentDTO.getMatiere()).orElse(null);
                        if (matiere == null) {
                            log.warn("Matière avec ID '{}' non trouvée lors de la mise à jour. Matière inchangée.", assignmentDTO.getMatiere());
                        } else {
                            assignment.setMatiere(matiere);
                        }
                    } catch (Exception e) {
                        log.warn("Erreur lors de la recherche de la matière '{}'. Matière inchangée: {}",
                                assignmentDTO.getMatiere(), e.getMessage());
                    }
                }
            }

            log.info("Sauvegarde de l'assignment mis à jour: {}", assignment);
            return assignmentRepository.save(assignment);
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de l'assignment: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void deleteAssignment(String id) {
        log.info("Suppression de l'assignment: {}", id);

        try {
            Assignment assignment = getAssignmentById(id);
            assignmentRepository.delete(assignment);
            log.info("Assignment supprimé avec succès: {}", id);
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de l'assignment: {}", e.getMessage(), e);
            throw e;
        }
    }


    public PaginatedResponse<Assignment> getSubmittedAssignments(int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "dateDeRendu"));
        Page<Assignment> assignmentsPage = assignmentRepository.findByRendu(true, pageable);

        return createPaginatedResponse(assignmentsPage);
    }

    public PaginatedResponse<Assignment> getPendingAssignments(int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.ASC, "dateDeRendu"));
        Page<Assignment> assignmentsPage = assignmentRepository.findByRendu(false, pageable);

        return createPaginatedResponse(assignmentsPage);
    }

    public PaginatedResponse<Assignment> getAssignmentsBySubject(String subjectId, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "dateDeRendu"));
        Page<Assignment> assignmentsPage = assignmentRepository.findByMatiereId(subjectId, pageable);

        return createPaginatedResponse(assignmentsPage);
    }

    public PaginatedResponse<Assignment> getAssignmentsByStudent(String studentId, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "dateDeRendu"));
        Page<Assignment> assignmentsPage = assignmentRepository.findByAuteurId(studentId, pageable);

        return createPaginatedResponse(assignmentsPage);
    }

    private <T> PaginatedResponse<T> createPaginatedResponse(Page<T> page) {
        return PaginatedResponse.<T>builder()
                .docs(page.getContent())
                .totalDocs(page.getTotalElements())
                .limit(page.getSize())
                .page(page.getNumber() + 1)
                .totalPages(page.getTotalPages())
                .pagingCounter((page.getNumber() * page.getSize()) + 1)
                .hasPrevPage(page.hasPrevious())
                .hasNextPage(page.hasNext())
                .prevPage(page.hasPrevious() ? page.getNumber() : null)
                .nextPage(page.hasNext() ? page.getNumber() + 2 : null)
                .build();
    }
}