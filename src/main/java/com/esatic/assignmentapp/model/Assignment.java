package com.esatic.assignmentapp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "assignments")
public class Assignment {

    @Id
    private String id;
    private String nom;
    private Date dateDeRendu;
    private boolean rendu;
    @DBRef
    private User auteur;
    @DBRef
    private Subject matiere;
    private Double note;
    private String remarques;
    private Date createdAt;
    private Date updatedAt;
    @DBRef
    private Class classId; // ID de la classe assignée
    private List<String> attachments; // URLs des fichiers attachés
}