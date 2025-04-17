package com.esatic.assignmentapp.controller;

import com.esatic.assignmentapp.model.Class;
import com.esatic.assignmentapp.model.User;
import com.esatic.assignmentapp.service.ClassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClassController {

    private final ClassService classService;

    @GetMapping
    public ResponseEntity<List<Class>> getAllClasses() {
        return ResponseEntity.ok(classService.getAllClasses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Class> getClassById(@PathVariable String id) {
        return ResponseEntity.ok(classService.getClassById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Class> createClass(@Valid @RequestBody Class classData) {
        return new ResponseEntity<>(classService.createClass(classData), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Class> updateClass(
            @PathVariable String id,
            @Valid @RequestBody Class classData
    ) {
        return ResponseEntity.ok(classService.updateClass(id, classData));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteClass(@PathVariable String id) {
        classService.deleteClass(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}/students")
    public ResponseEntity<List<User>> getStudentsByClass(@PathVariable String id) {
        return ResponseEntity.ok(classService.getStudentsByClassId(id));
    }

    @PostMapping("/{id}/students/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Class> addStudentToClass(
            @PathVariable String id,
            @PathVariable String studentId
    ) {
        return ResponseEntity.ok(classService.addStudentToClass(id, studentId));
    }

    @DeleteMapping("/{id}/students/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Class> removeStudentFromClass(
            @PathVariable String id,
            @PathVariable String studentId
    ) {
        return ResponseEntity.ok(classService.removeStudentFromClass(id, studentId));
    }
}