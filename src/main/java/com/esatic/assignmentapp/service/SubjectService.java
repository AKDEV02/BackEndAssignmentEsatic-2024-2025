package com.esatic.assignmentapp.service;

import com.esatic.assignmentapp.dto.SubjectDTO;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;  // Utilisez TeacherRepository au lieu de UserRepository

    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    public Subject getSubjectById(String id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", id));
    }

    public Subject createSubject(SubjectDTO subjectDTO) {
        log.info("Création d'une matière avec DTO: {}", subjectDTO);

        try {
            Subject subject = new Subject();
            subject.setName(subjectDTO.getName());
            subject.setImageUrl(subjectDTO.getImageUrl());
            subject.setColor(subjectDTO.getColor());
            subject.setDescription(subjectDTO.getDescription());
            subject.setCreatedAt(new Date());
            subject.setUpdatedAt(new Date());

            // Récupérer l'enseignant si un ID est fourni
            if (subjectDTO.getTeacherId() != null && !subjectDTO.getTeacherId().trim().isEmpty()) {
                log.info("Recherche de l'enseignant avec ID: {}", subjectDTO.getTeacherId());
                try {
                    Teacher teacher = teacherRepository.findById(subjectDTO.getTeacherId()).orElse(null);

                    if (teacher == null) {
                        log.warn("Enseignant avec ID '{}' non trouvé. La matière sera créée sans enseignant.",
                                subjectDTO.getTeacherId());
                    } else {
                        subject.setTeacher(teacher);
                        log.info("Enseignant associé: {}", teacher);
                    }
                } catch (Exception e) {
                    log.warn("Erreur lors de la recherche de l'enseignant '{}': {}",
                            subjectDTO.getTeacherId(), e.getMessage());
                }
            }

            log.info("Sauvegarde de la matière: {}", subject);
            return subjectRepository.save(subject);
        } catch (Exception e) {
            log.error("Erreur lors de la création de la matière: {}", e.getMessage(), e);
            throw e;
        }
    }

    public Subject updateSubject(String id, SubjectDTO subjectDTO) {
        log.info("Mise à jour de la matière {} avec DTO: {}", id, subjectDTO);

        try {
            Subject subject = getSubjectById(id);

            // Mettre à jour les propriétés
            subject.setName(subjectDTO.getName());
            subject.setImageUrl(subjectDTO.getImageUrl());
            subject.setColor(subjectDTO.getColor());
            subject.setDescription(subjectDTO.getDescription());
            subject.setUpdatedAt(new Date());

            // Mettre à jour l'enseignant si nécessaire
            if (subjectDTO.getTeacherId() != null) {
                if (subjectDTO.getTeacherId().trim().isEmpty()) {
                    // Si une chaîne vide est fournie, définir l'enseignant sur null
                    subject.setTeacher(null);
                    log.info("Enseignant défini sur null (chaîne vide fournie)");
                } else {
                    // Rechercher l'enseignant
                    try {
                        Teacher teacher = teacherRepository.findById(subjectDTO.getTeacherId()).orElse(null);

                        if (teacher == null) {
                            log.warn("Enseignant avec ID '{}' non trouvé lors de la mise à jour. Enseignant inchangé.",
                                    subjectDTO.getTeacherId());
                        } else {
                            subject.setTeacher(teacher);
                            log.info("Enseignant mis à jour: {}", teacher);
                        }
                    } catch (Exception e) {
                        log.warn("Erreur lors de la recherche de l'enseignant '{}': {}",
                                subjectDTO.getTeacherId(), e.getMessage());
                    }
                }
            }

            log.info("Sauvegarde de la matière mise à jour: {}", subject);
            return subjectRepository.save(subject);
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de la matière: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void deleteSubject(String id) {
        log.info("Suppression de la matière: {}", id);

        try {
            Subject subject = getSubjectById(id);
            subjectRepository.delete(subject);
            log.info("Matière supprimée avec succès: {}", id);
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de la matière: {}", e.getMessage(), e);
            throw e;
        }
    }
}