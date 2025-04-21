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
public class TeacherResponseDTO {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String photoUrl;
    private List<SubjectInfoDTO> subjects;
    private Date createdAt;
    private Date updatedAt;
}