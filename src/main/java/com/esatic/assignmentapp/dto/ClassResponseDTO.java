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
public class ClassResponseDTO {
    private String id;
    private String name;
    private String year;
    private String description;
    private List<StudentInfoDTO> students;
    private Date createdAt;
    private Date updatedAt;
}