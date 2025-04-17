package com.esatic.assignmentapp.controller;

import com.esatic.assignmentapp.model.User;
import com.esatic.assignmentapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        // Ne pas renvoyer les mots de passe
        users.forEach(user -> user.setPassword(null));
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.id == #id")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        User user = userService.getUserById(id);
        user.setPassword(null); // Ne pas renvoyer le mot de passe
        return ResponseEntity.ok(user);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        // Encoder le mot de passe
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            // Mot de passe par défaut si non fourni
            user.setPassword(passwordEncoder.encode("password"));
        }

        // Définir les dates
        Date now = new Date();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        // Activer le compte par défaut
        user.setEnabled(true);

        User savedUser = userService.saveUser(user);
        savedUser.setPassword(null); // Ne pas renvoyer le mot de passe

        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.id == #id")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User user) {
        User existingUser = userService.getUserById(id);

        // Mise à jour des champs autorisés
        if (user.getFirstName() != null) existingUser.setFirstName(user.getFirstName());
        if (user.getLastName() != null) existingUser.setLastName(user.getLastName());
        if (user.getEmail() != null) existingUser.setEmail(user.getEmail());
        if (user.getPhotoUrl() != null) existingUser.setPhotoUrl(user.getPhotoUrl());

        // Seul l'admin peut modifier certains champs
        if (user.getRole() != null && hasAdminRole()) {
            existingUser.setRole(user.getRole());
        }

        // Mettre à jour la date de modification
        existingUser.setUpdatedAt(new Date());

        User updatedUser = userService.saveUser(existingUser);
        updatedUser.setPassword(null); // Ne pas renvoyer le mot de passe

        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUserRole(@PathVariable String id, @RequestBody Map<String, String> payload) {
        String role = payload.get("role");
        if (role == null) {
            return ResponseEntity.badRequest().build();
        }

        User user = userService.getUserById(id);
        user.setRole(role);
        user.setUpdatedAt(new Date());

        User updatedUser = userService.saveUser(user);
        updatedUser.setPassword(null); // Ne pas renvoyer le mot de passe

        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> resetPassword(@PathVariable String id) {
        User user = userService.getUserById(id);

        // Générer un mot de passe aléatoire
        String newPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(new Date());

        userService.saveUser(user);

        // Dans un cas réel, vous enverriez le mot de passe par email
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Mot de passe réinitialisé avec succès");
        // Pour des tests, on renvoie le mot de passe généré - à ne pas faire en production !
        response.put("password", newPassword);

        return ResponseEntity.ok(response);
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    private boolean hasAdminRole() {
        return true; // À remplacer par une vérification réelle du rôle ADMIN
    }
}