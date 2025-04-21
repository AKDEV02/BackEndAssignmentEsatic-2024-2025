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
public class SubjectResponseDTO {
    private String id;
    private String name;
    private String imageUrl;
    private String teacherId;
    private String teacherName;
    private String color;
    private String description;
    private Date createdAt;
    private Date updatedAt;
}