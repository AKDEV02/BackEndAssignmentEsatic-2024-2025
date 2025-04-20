package com.esatic.assignmentapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentResponseDTO {
    private String id;
    private String nom;
    private Date dateDeRendu;
    private boolean rendu;
    private UserSimpleDTO auteur;
    private SubjectSimpleDTO matiere;
    private ClassSimpleDTO classId;
    private Double note;
    private String remarques;
    private List<String> attachments;
    private Date createdAt;
    private Date updatedAt;
}
