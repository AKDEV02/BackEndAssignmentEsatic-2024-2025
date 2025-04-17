package com.esatic.assignmentapp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "subjects")
public class Subject {

    @Id
    private String id;
    private String name;
    private String imageUrl;

    @DBRef
    private Teacher teacher;  // Garder Teacher comme type

    private String color;
    private String description;
    private Date createdAt;
    private Date updatedAt;
}