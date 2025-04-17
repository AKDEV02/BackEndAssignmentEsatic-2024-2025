package com.esatic.assignmentapp.controller;

import com.esatic.assignmentapp.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type) {

        try {
            String fileUrl = fileService.storeFile(file, type);

            Map<String, String> response = new HashMap<>();
            response.put("fileUrl", fileUrl);
            response.put("name", file.getOriginalFilename());
            response.put("message", "Fichier uploadé avec succès");

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            throw new RuntimeException("Échec de l'upload du fichier: " + e.getMessage());
        }
    }

    @GetMapping("/{type}/{filename:.+}")
    public ResponseEntity<Resource> serveFile(
            @PathVariable String type,
            @PathVariable String filename) {

        try {
            Resource file = fileService.loadFileAsResource(filename, type);

            String contentType = "application/octet-stream";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                    .body(file);
        } catch (IOException e) {
            throw new RuntimeException("Fichier non trouvé: " + e.getMessage());
        }
    }

    @PostMapping("/assignments/{assignmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> addFileToAssignment(
            @PathVariable String assignmentId,
            @RequestParam("file") MultipartFile file) {

        try {
            String fileUrl = fileService.storeFile(file, "assignment");
            fileService.addFileToAssignment(assignmentId, fileUrl);

            Map<String, String> response = new HashMap<>();
            response.put("fileUrl", fileUrl);
            response.put("name", file.getOriginalFilename());
            response.put("message", "Fichier ajouté au devoir avec succès");

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            throw new RuntimeException("Échec de l'upload du fichier: " + e.getMessage());
        }
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> deleteFile(@RequestParam("url") String fileUrl) {
        try {
            fileService.deleteFile(fileUrl);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Fichier supprimé avec succès");

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            throw new RuntimeException("Échec de la suppression du fichier: " + e.getMessage());
        }
    }
}