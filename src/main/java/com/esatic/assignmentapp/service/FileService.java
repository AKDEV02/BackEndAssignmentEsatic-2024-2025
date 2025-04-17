package com.esatic.assignmentapp.service;

import com.esatic.assignmentapp.exception.ResourceNotFoundException;
import com.esatic.assignmentapp.model.Assignment;
import com.esatic.assignmentapp.repository.AssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final Path profileRoot = Paths.get("uploads/profiles");
    private final Path assignmentRoot = Paths.get("uploads/assignments");
    private final AssignmentRepository assignmentRepository;

    public String storeFile(MultipartFile file, String type) throws IOException {
        // Initialisation des répertoires
        initStorageDirectories();

        // Validation du fichier
        if (file.isEmpty()) {
            throw new RuntimeException("Impossible de stocker un fichier vide");
        }

        // Génération d'un nom de fichier unique
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = "";
        if (originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID().toString() + fileExtension;

        // Sélection du répertoire de destination
        Path targetPath;
        if ("profile".equals(type)) {
            targetPath = profileRoot.resolve(newFilename);
        } else {
            targetPath = assignmentRoot.resolve(newFilename);
        }

        // Copie du fichier
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Retourner l'URL relative du fichier
        return "/uploads/" + type + "s/" + newFilename;
    }

    public Resource loadFileAsResource(String filename, String type) throws MalformedURLException {
        Path fileStorageLocation;
        if ("profile".equals(type)) {
            fileStorageLocation = profileRoot;
        } else {
            fileStorageLocation = assignmentRoot;
        }

        Path filePath = fileStorageLocation.resolve(filename).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists()) {
            return resource;
        } else {
            throw new RuntimeException("Fichier non trouvé: " + filename);
        }
    }

    public void addFileToAssignment(String assignmentId, String fileUrl) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment non trouvé avec l'ID: " + assignmentId));

        if (assignment.getAttachments() == null) {
            assignment.setAttachments(new ArrayList<>());
        }

        assignment.getAttachments().add(fileUrl);
        assignment.setUpdatedAt(new Date());
        assignmentRepository.save(assignment);
    }

    public void deleteFile(String fileUrl) throws IOException {
        // Extract filename from URL
        String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        Path path;

        if (fileUrl.contains("/profiles/")) {
            path = profileRoot.resolve(filename);
        } else if (fileUrl.contains("/assignments/")) {
            path = assignmentRoot.resolve(filename);
        } else {
            throw new RuntimeException("Type de fichier non pris en charge");
        }

        Files.deleteIfExists(path);
    }

    private void initStorageDirectories() throws IOException {
        if (!Files.exists(profileRoot)) {
            Files.createDirectories(profileRoot);
        }
        if (!Files.exists(assignmentRoot)) {
            Files.createDirectories(assignmentRoot);
        }
    }
}