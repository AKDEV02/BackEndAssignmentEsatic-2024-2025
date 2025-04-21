package com.esatic.assignmentapp.dto;

import lombok.Data;

import java.util.List;

@Data
public class AssignmentSubmissionDTO {
    private String studentId;
    private List<String> attachments;
}