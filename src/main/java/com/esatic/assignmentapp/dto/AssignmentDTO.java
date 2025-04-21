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
public class AssignmentDTO {
    private String id;
    private String nom;
    private Date dateDeRendu;
    private boolean rendu;
    private String auteurId;
    private String matiereId;
    private String matiereName;
    private String auteurName;
    private Double note;
    private String remarques;
    private String classId;
    private String className;
    private List<String> attachments;
    private Date createdAt;
    private Date updatedAt;
}