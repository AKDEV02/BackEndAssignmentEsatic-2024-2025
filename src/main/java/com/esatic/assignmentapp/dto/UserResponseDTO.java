package com.esatic.assignmentapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponseDTO {
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String photoUrl;

    private ClassInfoDTO classInfo; // pour les étudiants
    private List<SubjectInfoDTO> teachingSubjects; // pour les profs
}
