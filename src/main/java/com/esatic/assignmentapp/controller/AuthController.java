package com.esatic.assignmentapp.controller;

import com.esatic.assignmentapp.dto.AuthenticationRequest;
import com.esatic.assignmentapp.dto.AuthenticationResponse;
import com.esatic.assignmentapp.dto.ErrorResponse;
import com.esatic.assignmentapp.dto.RegisterRequest;
import com.esatic.assignmentapp.model.User;
import com.esatic.assignmentapp.service.AuthenticationService;
import com.esatic.assignmentapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
public class AuthController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateProfile(
            @RequestBody Map<String, Object> payload
    ) {
        try {
            String userId = SecurityContextHolder.getContext().getAuthentication().getName();
            User updatedUser = userService.updateProfile(userId, payload);

            // Construire une réponse sans le mot de passe
            updatedUser.setPassword(null);

            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors de la mise à jour du profil", e.getMessage()));
        }
    }

    @PutMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(
            @RequestBody Map<String, String> payload
    ) {
        try {
            String userId = SecurityContextHolder.getContext().getAuthentication().getName();
            String currentPassword = payload.get("currentPassword");
            String newPassword = payload.get("newPassword");

            userService.changePassword(userId, currentPassword, newPassword);

            return ResponseEntity.ok().body(Map.of("message", "Mot de passe mis à jour avec succès"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors du changement de mot de passe", e.getMessage()));
        }
    }

    // Endpoint de test pour vérifier CORS
    @GetMapping("/test")
    public ResponseEntity<String> testCors() {
        return ResponseEntity.ok("CORS est correctement configuré!");
    }
}