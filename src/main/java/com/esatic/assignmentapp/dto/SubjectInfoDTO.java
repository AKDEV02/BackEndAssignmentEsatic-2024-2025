package com.esatic.assignmentapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectInfoDTO {
    private String id;
    private String name;
    private String imageUrl;
    private String color;
}