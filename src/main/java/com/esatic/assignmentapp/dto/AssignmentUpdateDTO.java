package com.esatic.assignmentapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentUpdateDTO {
    @NotBlank
    private String nom;
    @NotNull
    private Date dateDeRendu;
    private boolean rendu;
    private String auteurId;
    private String matiereId;
    private String classId;
    private Double note;
    private String remarques;
}