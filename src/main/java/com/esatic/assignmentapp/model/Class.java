package com.esatic.assignmentapp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "classes")
public class Class {
    @Id
    private String id;
    private String name;
    private String year;
    private String description;
    private Date createdAt;
    private Date updatedAt;
}