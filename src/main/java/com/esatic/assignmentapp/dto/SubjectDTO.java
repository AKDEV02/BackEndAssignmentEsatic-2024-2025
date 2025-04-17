package com.esatic.assignmentapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectDTO {
    private String id;
    private String name;
    private String imageUrl;
    private String teacherId;  // ID de l'enseignant au lieu de l'objet complet
    private String color;
    private String description;
}