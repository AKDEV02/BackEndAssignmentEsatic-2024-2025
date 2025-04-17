package com.esatic.assignmentapp.config;

import com.esatic.assignmentapp.model.Assignment;
import com.esatic.assignmentapp.model.Subject;
import com.esatic.assignmentapp.model.Teacher;
import com.esatic.assignmentapp.model.User;
import com.esatic.assignmentapp.repository.AssignmentRepository;
import com.esatic.assignmentapp.repository.SubjectRepository;
import com.esatic.assignmentapp.repository.TeacherRepository;
import com.esatic.assignmentapp.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/data-generator")
public class MockarooDataGenerator implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final AssignmentRepository assignmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.mockaroo.api-key:e04277b0}")
    private String mockarooApiKey;

    @Value("${app.mockaroo.cache-directory:./mock-data}")
    private String cacheDirectory;

    @Value("${app.mockaroo.use-cache:true}")
    private boolean useCache;

    @Value("${app.mockaroo.regenerate:false}")
    private boolean regenerateData;

    private static final int ADMINS_COUNT = 3;
    private static final int TEACHERS_COUNT = 20;
    private static final int SUBJECTS_COUNT = 40;
    private static final int STUDENTS_COUNT = 60;
    private static final int ASSIGNMENTS_COUNT = 100;
    private static final int BATCH_SIZE = 20;

    @Override
    public void run(String... args) {
        log.info("MockarooDataGenerator prêt - utilisez l'API pour générer des données");
        // Ne pas exécuter la génération au démarrage pour éviter les problèmes
    }

    @GetMapping("/init")
    public Map<String, Object> initializeData() {
        log.info("Démarrage de la génération de données avec Mockaroo via API...");

        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();

        try {
            result.put("success", true);
            result.put("message", "Génération de données démarrée");

            // Exécuter la génération dans un thread séparé pour ne pas bloquer la réponse API
            new Thread(() -> {
                try {
                    generateData();
                    log.info("Génération de données via API terminée avec succès");
                } catch (Exception e) {
                    log.error("Erreur lors de la génération des données via API", e);
                }
            }).start();

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Erreur: " + e.getMessage());
            log.error("Erreur lors du démarrage de la génération des données", e);
        }

        return result;
    }

    private void generateData() {
        // Vérifier si la base de données est vide
        if (userRepository.count() > 0 && !regenerateData) {
            log.info("La base de données contient déjà des données. Génération ignorée.");
            return;
        }

        // Nettoyer les données existantes si regenerateData est vrai
        if (regenerateData) {
            cleanupExistingData();
        }

        // Créer le répertoire de cache si nécessaire
        createCacheDirectory();

        long startTime = System.currentTimeMillis();

        try {
            // 1. Créer les administrateurs
            List<User> admins = createAdmins();
            log.info("{} administrateurs créés", admins.size());

            // 2. Créer les enseignants
            List<Teacher> teachers = createTeachers();
            log.info("{} enseignants créés", teachers.size());

            // 3. Créer les matières
            List<Subject> subjects = createSubjects(teachers);
            log.info("{} matières créées", subjects.size());

            // 4. Créer les étudiants
            List<User> students = createStudents();
            log.info("{} étudiants créés", students.size());

            // 5. Créer les devoirs
            createAssignments(subjects, students);
            log.info("Devoirs créés avec succès");

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            log.info("Génération de données terminée en {} secondes !",
                    TimeUnit.MILLISECONDS.toSeconds(duration));

        } catch (Exception e) {
            log.error("Erreur lors de la génération des données", e);
        }
    }

    private void cleanupExistingData() {
        log.info("Nettoyage des données existantes...");
        assignmentRepository.deleteAll();
        subjectRepository.deleteAll();
        teacherRepository.deleteAll();
        userRepository.deleteAll();
        log.info("Nettoyage des données terminé.");
    }

    private void createCacheDirectory() {
        File directory = new File(cacheDirectory);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                log.info("Répertoire de cache créé : {}", cacheDirectory);
            } else {
                log.warn("Impossible de créer le répertoire de cache : {}", cacheDirectory);
            }
        }
    }

    private List<User> createAdmins() throws IOException {
        log.info("Création de {} administrateurs...", ADMINS_COUNT);

        List<User> admins = new ArrayList<>();

        // Créer un administrateur par défaut avec identifiants connus
        User defaultAdmin = User.builder()
                .firstName("Admin")
                .lastName("Principal")
                .username("admin")
                .email("admin@example.com")
                .password(passwordEncoder.encode("admin123"))
                .role("ADMIN")
                .enabled(true)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        admins.add(defaultAdmin);

        // Récupérer des données Mockaroo pour les autres administrateurs
        if (ADMINS_COUNT > 1) {
            String cacheFile = cacheDirectory + "/admins.json";
            List<Map<String, Object>> adminData;

            if (shouldUseCache(cacheFile)) {
                adminData = readFromCache(cacheFile);
            } else {
                adminData = fetchDataFromMockaroo("admins", ADMINS_COUNT - 1);
                saveToCache(adminData, cacheFile);
            }

            for (int i = 0; i < adminData.size(); i++) {
                Map<String, Object> data = adminData.get(i);
                User admin = User.builder()
                        .firstName((String) data.get("first_name"))
                        .lastName((String) data.get("last_name"))
                        .username("admin" + (i + 2))
                        .email(data.get("email").toString())
                        .password(passwordEncoder.encode("password"))
                        .role("ADMIN")
                        .photoUrl(data.get("avatar").toString())
                        .enabled(true)
                        .createdAt(new Date())
                        .updatedAt(new Date())
                        .build();

                admins.add(admin);
            }
        }

        return userRepository.saveAll(admins);
    }

    private List<Teacher> createTeachers() throws IOException {
        log.info("Création de {} enseignants...", TEACHERS_COUNT);

        String cacheFile = cacheDirectory + "/teachers.json";
        List<Map<String, Object>> teacherData;

        if (shouldUseCache(cacheFile)) {
            teacherData = readFromCache(cacheFile);
        } else {
            teacherData = fetchDataFromMockaroo("teachers", TEACHERS_COUNT);
            saveToCache(teacherData, cacheFile);
        }

        List<Teacher> teachers = new ArrayList<>();

        for (int i = 0; i < teacherData.size(); i += BATCH_SIZE) {
            int batchEnd = Math.min(i + BATCH_SIZE, teacherData.size());
            List<Teacher> batch = new ArrayList<>();

            for (int j = i; j < batchEnd; j++) {
                Map<String, Object> data = teacherData.get(j);
                Teacher teacher = Teacher.builder()
                        .firstName((String) data.get("first_name"))
                        .lastName((String) data.get("last_name"))
                        .email(data.get("email").toString())
                        .photoUrl(data.get("avatar").toString())
                        .subjects(new ArrayList<>())
                        .createdAt(new Date())
                        .updatedAt(new Date())
                        .build();

                batch.add(teacher);
            }

            teachers.addAll(teacherRepository.saveAll(batch));
            log.info("Lot de {} enseignants créé ({}/{})", batch.size(), teachers.size(), TEACHERS_COUNT);
        }

        return teachers;
    }

    private List<Subject> createSubjects(List<Teacher> teachers) throws IOException {
        log.info("Création de {} matières...", SUBJECTS_COUNT);

        String cacheFile = cacheDirectory + "/subjects.json";
        List<Map<String, Object>> subjectData;

        if (shouldUseCache(cacheFile)) {
            subjectData = readFromCache(cacheFile);
        } else {
            subjectData = fetchDataFromMockaroo("subjects", SUBJECTS_COUNT);
            saveToCache(subjectData, cacheFile);
        }

        List<Subject> subjects = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < subjectData.size(); i += BATCH_SIZE) {
            int batchEnd = Math.min(i + BATCH_SIZE, subjectData.size());
            List<Subject> batch = new ArrayList<>();

            for (int j = i; j < batchEnd; j++) {
                Map<String, Object> data = subjectData.get(j);
                Teacher teacher = teachers.get(random.nextInt(teachers.size()));

                Subject subject = Subject.builder()
                        .name((String) data.get("course_name"))
                        .imageUrl(data.get("image_url").toString())
                        .teacher(teacher)
                        .color(data.get("color").toString())
                        .description((String) data.get("description"))
                        .createdAt(new Date())
                        .updatedAt(new Date())
                        .build();

                batch.add(subject);
            }

            subjects.addAll(subjectRepository.saveAll(batch));
            log.info("Lot de {} matières créé ({}/{})", batch.size(), subjects.size(), SUBJECTS_COUNT);
        }

        // Mettre à jour les enseignants avec leurs matières
        updateTeachersWithSubjects(teachers, subjects);

        return subjects;
    }

    private void updateTeachersWithSubjects(List<Teacher> teachers, List<Subject> subjects) {
        log.info("Mise à jour des enseignants avec leurs matières associées...");

        Map<String, List<String>> teacherSubjects = new HashMap<>();

        // Parcourir toutes les matières et les attribuer à leurs enseignants
        for (Subject subject : subjects) {
            if (subject.getTeacher() != null) {
                String teacherId = subject.getTeacher().getId();
                teacherSubjects.computeIfAbsent(teacherId, k -> new ArrayList<>())
                        .add(subject.getName());
            }
        }

        // Mettre à jour chaque enseignant avec sa liste de matières
        List<Teacher> updatedTeachers = new ArrayList<>();

        for (Teacher teacher : teachers) {
            List<String> subjectsList = teacherSubjects.get(teacher.getId());
            if (subjectsList != null && !subjectsList.isEmpty()) {
                teacher.setSubjects(subjectsList);
                updatedTeachers.add(teacher);
            }
        }

        // Sauvegarder par lots
        for (int i = 0; i < updatedTeachers.size(); i += BATCH_SIZE) {
            int batchEnd = Math.min(i + BATCH_SIZE, updatedTeachers.size());
            List<Teacher> batch = updatedTeachers.subList(i, batchEnd);
            teacherRepository.saveAll(batch);
            log.info("Lot de {} enseignants mis à jour avec leurs matières ({}/{})",
                    batch.size(), batchEnd, updatedTeachers.size());
        }
    }

    private List<User> createStudents() throws IOException {
        log.info("Création de {} étudiants...", STUDENTS_COUNT);

        String cacheFile = cacheDirectory + "/students.json";
        List<Map<String, Object>> studentData;

        if (shouldUseCache(cacheFile)) {
            studentData = readFromCache(cacheFile);
        } else {
            studentData = fetchDataFromMockaroo("students", STUDENTS_COUNT);
            saveToCache(studentData, cacheFile);
        }

        List<User> students = new ArrayList<>();

        for (int i = 0; i < studentData.size(); i += BATCH_SIZE) {
            int batchEnd = Math.min(i + BATCH_SIZE, studentData.size());
            List<User> batch = new ArrayList<>();

            for (int j = i; j < batchEnd; j++) {
                Map<String, Object> data = studentData.get(j);
                User student = User.builder()
                        .firstName((String) data.get("first_name"))
                        .lastName((String) data.get("last_name"))
                        .username("student" + (j + 1))
                        .email(data.get("email").toString())
                        .password(passwordEncoder.encode("password"))
                        .role("STUDENT")
                        .photoUrl(data.get("avatar").toString())
                        .enabled(true)
                        .createdAt(new Date())
                        .updatedAt(new Date())
                        .build();

                batch.add(student);
            }

            students.addAll(userRepository.saveAll(batch));
            log.info("Lot de {} étudiants créé ({}/{})", batch.size(), students.size(), STUDENTS_COUNT);
        }

        return students;
    }

    private void createAssignments(List<Subject> subjects, List<User> students) throws IOException {
        log.info("Création de {} devoirs...", ASSIGNMENTS_COUNT);

        String cacheFile = cacheDirectory + "/assignments.json";
        List<Map<String, Object>> assignmentData;

        if (shouldUseCache(cacheFile)) {
            assignmentData = readFromCache(cacheFile);
        } else {
            assignmentData = fetchDataFromMockaroo("assignments", ASSIGNMENTS_COUNT);
            saveToCache(assignmentData, cacheFile);
        }

        Random random = new Random();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        for (int i = 0; i < assignmentData.size(); i += BATCH_SIZE) {
            int batchEnd = Math.min(i + BATCH_SIZE, assignmentData.size());
            List<Assignment> batch = new ArrayList<>();

            for (int j = i; j < batchEnd; j++) {
                Map<String, Object> data = assignmentData.get(j);

                User student = students.get(random.nextInt(students.size()));
                Subject subject = subjects.get(random.nextInt(subjects.size()));
                boolean rendu = (boolean) data.get("rendu");

                try {
                    Date dateDeRendu = dateFormat.parse((String) data.get("date_de_rendu"));

                    Assignment assignment = Assignment.builder()
                            .nom((String) data.get("nom"))
                            .dateDeRendu(dateDeRendu)
                            .rendu(rendu)
                            .auteur(student)
                            .matiere(subject)
                            .note(rendu ? (Double) data.get("note") : null)
                            .remarques(rendu ? (String) data.get("remarques") : null)
                            .createdAt(new Date())
                            .updatedAt(new Date())
                            .build();

                    batch.add(assignment);
                } catch (Exception e) {
                    log.error("Erreur lors de la création d'un devoir : ", e);
                }
            }

            assignmentRepository.saveAll(batch);
            log.info("Lot de {} devoirs créé ({}/{})", batch.size(), batchEnd, ASSIGNMENTS_COUNT);
        }
    }

    private List<Map<String, Object>> fetchDataFromMockaroo(String schemaName, int count) {
        log.info("Récupération de {} données depuis Mockaroo pour le schéma '{}'...", count, schemaName);

        String url = "https://my.api.mockaroo.com/" + schemaName + ".json?key=" + mockarooApiKey + "&count=" + count;

        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des données depuis Mockaroo", e);
            return new ArrayList<>();
        }
    }

    private boolean shouldUseCache(String cacheFile) {
        if (!useCache || regenerateData) {
            return false;
        }

        Path path = Paths.get(cacheFile);
        return Files.exists(path);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> readFromCache(String cacheFile) throws IOException {
        log.info("Lecture des données depuis le cache : {}", cacheFile);

        File file = new File(cacheFile);
        return objectMapper.readValue(file, List.class);
    }

    private void saveToCache(List<Map<String, Object>> data, String cacheFile) throws IOException {
        log.info("Sauvegarde des données dans le cache : {}", cacheFile);

        File file = new File(cacheFile);
        objectMapper.writeValue(file, data);
    }
}