package com.esatic.assignmentapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentDTO {
    private String id;
    private String nom;
    private Date dateDeRendu;
    private boolean rendu;
    private String auteur; // Peut être un ID ou un nom
    private String matiere; // Peut être un ID ou un nom
    private Double note;
    private String remarques;
}