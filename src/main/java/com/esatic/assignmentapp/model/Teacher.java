package com.esatic.assignmentapp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "teachers")
public class Teacher {

    @Id
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String photoUrl;
    private List<String> subjects;
    private Date createdAt;
    private Date updatedAt;
}