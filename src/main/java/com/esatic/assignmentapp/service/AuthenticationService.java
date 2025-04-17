package com.esatic.assignmentapp.service;

import com.esatic.assignmentapp.dto.AuthenticationRequest;
import com.esatic.assignmentapp.dto.AuthenticationResponse;
import com.esatic.assignmentapp.dto.RegisterRequest;
import com.esatic.assignmentapp.model.User;
import com.esatic.assignmentapp.repository.UserRepository;
import com.esatic.assignmentapp.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
        // Vérifier si le nom d'utilisateur ou l'email existe déjà
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Ce nom d'utilisateur est déjà pris");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Cette adresse email est déjà utilisée");
        }

        // Créer un nouvel utilisateur
        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : "STUDENT")
                .enabled(true)  // Définir explicitement enabled à true
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        var savedUser = userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);

        // Ne pas renvoyer le mot de passe dans la réponse
        savedUser.setPassword(null);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .user(savedUser)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // Authentifier l'utilisateur
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Utilisateur authentifié avec succès, générer un token
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);

        // Ne pas renvoyer le mot de passe dans la réponse
        user.setPassword(null);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .user(user)
                .build();
    }
}