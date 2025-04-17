package com.esatic.assignmentapp.service;

import com.esatic.assignmentapp.model.Class;
import com.esatic.assignmentapp.model.Subject;
import com.esatic.assignmentapp.model.User;
import com.esatic.assignmentapp.repository.ClassRepository;
import com.esatic.assignmentapp.repository.SubjectRepository;
import com.esatic.assignmentapp.repository.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClassRepository classRepository;
    private final SubjectRepository subjectRepository;

    public UserService(
            UserRepository userRepository,
            @Lazy PasswordEncoder passwordEncoder,
            ClassRepository classRepository,
            SubjectRepository subjectRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.classRepository = classRepository;
        this.subjectRepository = subjectRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + username));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'id: " + id));
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User updateProfile(String userId, Map<String, Object> payload) {
        User user = getUserById(userId);

        if (payload.containsKey("firstName")) {
            user.setFirstName((String) payload.get("firstName"));
        }

        if (payload.containsKey("lastName")) {
            user.setLastName((String) payload.get("lastName"));
        }

        if (payload.containsKey("email")) {
            String newEmail = (String) payload.get("email");
            // Vérifier si l'email n'est pas déjà utilisé par un autre utilisateur
            if (!user.getEmail().equals(newEmail) && existsByEmail(newEmail)) {
                throw new IllegalArgumentException("Cet email est déjà utilisé par un autre compte");
            }
            user.setEmail(newEmail);
        }

        if (payload.containsKey("photoUrl")) {
            user.setPhotoUrl((String) payload.get("photoUrl"));
        }

        if (payload.containsKey("classId") && "STUDENT".equalsIgnoreCase(user.getRole())) {
            String classId = (String) payload.get("classId");
            Class studentClass = classRepository.findById(classId)
                    .orElseThrow(() -> new IllegalArgumentException("Classe non trouvée avec l'id: " + classId));
            user.setClassId(studentClass);
        }

        if (payload.containsKey("teachingSubjects") && "TEACHER".equalsIgnoreCase(user.getRole())) {
            List<String> subjectIds = (List<String>) payload.get("teachingSubjects");
            List<Subject> subjects = new ArrayList<>();

            for (String subjectId : subjectIds) {
                Subject subject = subjectRepository.findById(subjectId)
                        .orElseThrow(() -> new IllegalArgumentException("Matière non trouvée avec l'id: " + subjectId));
                subjects.add(subject);
            }

            user.setTeachingSubjects(subjects);
        }

        // Mettre à jour la date de modification
        user.setUpdatedAt(new Date());

        return userRepository.save(user);
    }

    public void changePassword(String userId, String currentPassword, String newPassword) {
        User user = getUserById(userId);

        // Vérifier si le mot de passe actuel est correct
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadCredentialsException("Le mot de passe actuel est incorrect");
        }

        // Encoder et définir le nouveau mot de passe
        user.setPassword(passwordEncoder.encode(newPassword));

        // Mettre à jour la date de modification
        user.setUpdatedAt(new Date());

        userRepository.save(user);
    }
}