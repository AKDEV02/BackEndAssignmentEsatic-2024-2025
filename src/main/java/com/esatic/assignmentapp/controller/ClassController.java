package com.esatic.assignmentapp.controller;

import com.esatic.assignmentapp.dto.ClassDTO;
import com.esatic.assignmentapp.dto.ClassResponseDTO;
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
    public ResponseEntity<List<ClassResponseDTO>> getAllClasses() {
        return ResponseEntity.ok(classService.getAllClasses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassResponseDTO> getClassById(@PathVariable String id) {
        return ResponseEntity.ok(classService.getClassById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClassResponseDTO> createClass(@Valid @RequestBody ClassDTO classDTO) {
        return new ResponseEntity<>(classService.createClass(classDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClassResponseDTO> updateClass(
            @PathVariable String id,
            @Valid @RequestBody ClassDTO classDTO) {
        return ResponseEntity.ok(classService.updateClass(id, classDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteClass(@PathVariable String id) {
        classService.deleteClass(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/students/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClassResponseDTO> addStudentToClass(
            @PathVariable String id,
            @PathVariable String studentId) {
        return ResponseEntity.ok(classService.addStudentToClass(id, studentId));
    }

    @DeleteMapping("/{id}/students/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClassResponseDTO> removeStudentFromClass(
            @PathVariable String id,
            @PathVariable String studentId) {
        return ResponseEntity.ok(classService.removeStudentFromClass(id, studentId));
    }
}