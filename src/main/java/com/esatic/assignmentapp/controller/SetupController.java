package com.esatic.assignmentapp.controller;

import com.esatic.assignmentapp.model.Assignment;
import com.esatic.assignmentapp.model.Class;
import com.esatic.assignmentapp.model.Subject;
import com.esatic.assignmentapp.model.User;
import com.esatic.assignmentapp.repository.AssignmentRepository;
import com.esatic.assignmentapp.repository.ClassRepository;
import com.esatic.assignmentapp.repository.SubjectRepository;
import com.esatic.assignmentapp.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/setup")
public class SetupController {

    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final AssignmentRepository assignmentRepository;
    private final ClassRepository classRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private boolean useDataCache = true;

    @Value("${app.mockaroo.api-key:6bddbd20}")
    private String mockarooApiKey;

    @Value("${app.mockaroo.cache-directory:./mock-data}")
    private String cacheDirectory;

    @Autowired
    public SetupController(UserRepository userRepository,
                           SubjectRepository subjectRepository,
                           AssignmentRepository assignmentRepository,
                           ClassRepository classRepository,
                           PasswordEncoder passwordEncoder,
                           RestTemplate restTemplate,
                           ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.assignmentRepository = assignmentRepository;
        this.classRepository = classRepository;
        this.passwordEncoder = passwordEncoder;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/init")
    public ResponseEntity<Map<String, Object>> initializeTestData(
            @RequestParam(value = "force", defaultValue = "false") boolean force) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Vérifier si des données existent déjà
            if (userRepository.count() > 0 && !force) {
                response.put("success", false);
                response.put("message", "Des données existent déjà dans la base de données. Utilisez ?force=true pour réinitialiser.");
                return ResponseEntity.ok(response);
            }

            // Si force=true, supprimer toutes les données existantes
            if (force) {
                assignmentRepository.deleteAll();
                subjectRepository.deleteAll();
                classRepository.deleteAll();
                userRepository.deleteAll();
                response.put("cleaned", true);
            }

            // 1. Créer un utilisateur admin
            User admin = User.builder()
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

            User savedAdmin = userRepository.save(admin);

            // 2. Créer une classe
            Class classe = Class.builder()
                    .name("Classe Test")
                    .year("2024-2025")
                    .description("Classe utilisée pour les tests")
                    .students(new ArrayList<>())
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .build();

            Class savedClass = classRepository.save(classe);

            // 3. Créer un étudiant
            User student = User.builder()
                    .firstName("Étudiant")
                    .lastName("Test")
                    .username("student")
                    .email("student@example.com")
                    .password(passwordEncoder.encode("password"))
                    .role("STUDENT")
                    .classId(savedClass)
                    .enabled(true)
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .build();

            User savedStudent = userRepository.save(student);

            // Mettre à jour la classe avec l'étudiant
            savedClass.getStudents().add(savedStudent);
            classRepository.save(savedClass);

            // 4. Créer un enseignant
            User teacher = User.builder()
                    .firstName("Professeur")
                    .lastName("Martin")
                    .username("teacher")
                    .email("prof.martin@example.com")
                    .password(passwordEncoder.encode("password"))
                    .role("TEACHER")
                    .photoUrl("https://randomuser.me/api/portraits/men/42.jpg")
                    .teachingSubjects(new ArrayList<>())
                    .enabled(true)
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .build();

            User savedTeacher = userRepository.save(teacher);

            // 5. Créer quelques matières
            List<Subject> subjects = new ArrayList<>();

            Subject mathSubject = Subject.builder()
                    .name("Mathématiques")
                    .description("Cours de mathématiques avancées")
                    .color("#4285F4")  // Bleu
                    .imageUrl("https://img.icons8.com/color/96/000000/mathematics.png")
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .build();

            subjects.add(mathSubject);

            Subject infoSubject = Subject.builder()
                    .name("Informatique")
                    .description("Introduction à la programmation")
                    .color("#34A853")  // Vert
                    .imageUrl("https://img.icons8.com/color/96/000000/code.png")
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .build();

            subjects.add(infoSubject);

            Subject physicsSubject = Subject.builder()
                    .name("Physique")
                    .description("Principes fondamentaux de la physique")
                    .color("#FBBC05")  // Jaune
                    .imageUrl("https://img.icons8.com/color/96/000000/physics.png")
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .build();

            subjects.add(physicsSubject);

            List<Subject> savedSubjects = subjectRepository.saveAll(subjects);

            // 6. Ajouter les matières au professeur
            savedTeacher.setTeachingSubjects(savedSubjects);
            userRepository.save(savedTeacher);

            // 7. Créer quelques devoirs
            List<Assignment> assignments = new ArrayList<>();

            // Devoir rendu
            Assignment assignment1 = Assignment.builder()
                    .nom("Exercices d'algèbre")
                    .dateDeRendu(new Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000))  // 7 jours avant
                    .rendu(true)
                    .auteur(savedStudent)
                    .matiere(savedSubjects.get(0))  // Mathématiques
                    .classId(savedClass)
                    .note(15.5)
                    .remarques("Bon travail, quelques erreurs mineures.")
                    .attachments(new ArrayList<>())
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .build();

            assignments.add(assignment1);

            // Devoir à rendre
            Assignment assignment2 = Assignment.builder()
                    .nom("Projet de programmation")
                    .dateDeRendu(new Date(System.currentTimeMillis() + 14 * 24 * 60 * 60 * 1000))  // Dans 14 jours
                    .rendu(false)
                    .auteur(savedStudent)
                    .matiere(savedSubjects.get(1))  // Informatique
                    .classId(savedClass)
                    .attachments(new ArrayList<>())
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .build();

            assignments.add(assignment2);

            assignmentRepository.saveAll(assignments);

            response.put("success", true);
            response.put("message", "Données initiales créées avec succès !");
            response.put("details", Map.of(
                    "users", 3,
                    "classes", 1,
                    "subjects", subjects.size(),
                    "assignments", assignments.size()
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la création des données : " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/init-mockaroo")
    public ResponseEntity<Map<String, Object>> initializeMockarooData(
            @RequestParam(value = "force", defaultValue = "false") boolean force,
            @RequestParam(value = "useCache", defaultValue = "true") boolean useCache,
            @RequestParam(value = "admins", defaultValue = "5") int admins,
            @RequestParam(value = "teachers", defaultValue = "30") int teachers,
            @RequestParam(value = "subjects", defaultValue = "50") int subjects,
            @RequestParam(value = "students", defaultValue = "100") int students,
            @RequestParam(value = "classes", defaultValue = "50") int classes,
            @RequestParam(value = "assignments", defaultValue = "1000") int assignments) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Vérifier si des données existent déjà
            if (userRepository.count() > 0 && !force) {
                response.put("success", false);
                response.put("message", "Des données existent déjà dans la base de données. Utilisez ?force=true pour réinitialiser.");
                return ResponseEntity.ok(response);
            }

            // Si force=true, supprimer toutes les données existantes
            if (force) {
                assignmentRepository.deleteAll();
                subjectRepository.deleteAll();
                classRepository.deleteAll();
                userRepository.deleteAll();
                response.put("cleaned", true);
            }

            // Définir si on utilise le cache ou non
            this.useDataCache = useCache;

            // Lancer la génération dans un thread séparé pour ne pas bloquer la réponse
            new Thread(() -> {
                try {
                    generateMockarooData(admins, teachers, subjects, students, classes, assignments);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            response.put("success", true);
            response.put("message", "Génération des données Mockaroo démarrée");
            response.put("params", Map.of(
                    "admins", admins,
                    "teachers", teachers,
                    "subjects", subjects,
                    "students", students,
                    "classes", classes,
                    "assignments", assignments,
                    "useCache", useCache
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/delete-cache")
    public ResponseEntity<Map<String, Object>> deleteCache() {
        Map<String, Object> response = new HashMap<>();

        try {
            File directory = new File(cacheDirectory);
            if (directory.exists()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        boolean deleted = file.delete();
                        System.out.println("Fichier " + file.getName() + " supprimé: " + deleted);
                    }
                }
                boolean directoryDeleted = directory.delete();
                response.put("success", directoryDeleted);
                response.put("message", "Cache supprimé: " + directoryDeleted);
            } else {
                response.put("success", true);
                response.put("message", "Le répertoire de cache n'existe pas.");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la suppression du cache: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    private void generateMockarooData(int adminsCount, int teachersCount, int subjectsCount, int studentsCount, int classesCount, int assignmentsCount) {
        try {
            System.out.println("Démarrage de la génération de données Mockaroo...");

            // Créer le répertoire de cache si nécessaire
            createCacheDirectory();

            long startTime = System.currentTimeMillis();

            // 1. Créer les administrateurs
            List<User> admins = createAdmins(adminsCount);
            System.out.println(admins.size() + " administrateurs créés");

            // 2. Créer les classes
            List<Class> classes = createClasses(classesCount);
            System.out.println(classes.size() + " classes créées");

            // 3. Créer les enseignants
            List<User> teachers = createTeachers(teachersCount);
            System.out.println(teachers.size() + " enseignants créés");

            // 4. Créer les matières
            List<Subject> subjects = createSubjects(subjectsCount);
            System.out.println(subjects.size() + " matières créées");

            // 5. Attribuer des matières aux enseignants
            updateTeachersWithSubjects(teachers, subjects);

            // 6. Créer les étudiants
            List<User> students = createStudents(studentsCount, classes);
            System.out.println(students.size() + " étudiants créés");

            // 7. Mettre à jour les classes avec leurs étudiants
            updateClassesWithStudents(classes, students);

            // 8. Créer les devoirs
            createAssignments(subjects, students, classes, assignmentsCount);
            System.out.println(assignmentsCount + " devoirs créés");

            long endTime = System.currentTimeMillis();
            long duration = TimeUnit.MILLISECONDS.toSeconds(endTime - startTime);

            System.out.println("Génération des données Mockaroo terminée en " + duration + " secondes");
        } catch (Exception e) {
            System.err.println("Erreur lors de la génération des données Mockaroo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createCacheDirectory() {
        File directory = new File(cacheDirectory);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                System.out.println("Répertoire de cache créé : " + cacheDirectory);
            } else {
                System.out.println("Impossible de créer le répertoire de cache : " + cacheDirectory);
            }
        }
    }

    private List<User> createAdmins(int count) throws IOException {
        System.out.println("Création de " + count + " administrateurs...");

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
        if (count > 1) {
            String cacheFile = cacheDirectory + "/admins.json";
            List<Map<String, Object>> adminData;

            if (shouldUseCache(cacheFile)) {
                adminData = readFromCache(cacheFile);
            } else {
                adminData = fetchDataFromMockaroo("admins", count - 1);
                saveToCache(adminData, cacheFile);
            }

            for (int i = 0; i < Math.min(adminData.size(), count - 1); i++) {
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

    private List<Class> createClasses(int count) throws IOException {
        System.out.println("Création de " + count + " classes...");

        String cacheFile = cacheDirectory + "/classes.json";
        List<Map<String, Object>> classesData;

        if (shouldUseCache(cacheFile)) {
            classesData = readFromCache(cacheFile);
        } else {
            classesData = fetchDataFromMockaroo("classes", count);
            saveToCache(classesData, cacheFile);
        }

        List<Class> classes = new ArrayList<>();
        int batchSize = 20;

        for (int i = 0; i < Math.min(classesData.size(), count); i += batchSize) {
            int batchEnd = Math.min(i + batchSize, Math.min(classesData.size(), count));
            List<Class> batch = new ArrayList<>();

            for (int j = i; j < batchEnd; j++) {
                Map<String, Object> data = classesData.get(j);

                Class classEntity = Class.builder()
                        .name((String) data.get("name"))
                        .year((String) data.get("year"))
                        .description((String) data.get("description"))
                        .students(new ArrayList<>())
                        .createdAt(new Date())
                        .updatedAt(new Date())
                        .build();

                batch.add(classEntity);
            }

            classes.addAll(classRepository.saveAll(batch));
            System.out.println("Lot de " + batch.size() + " classes créées (" + classes.size() + "/" + count + ")");
        }

        return classes;
    }

    private List<User> createTeachers(int count) throws IOException {
        System.out.println("Création de " + count + " enseignants...");

        String cacheFile = cacheDirectory + "/teachers.json";
        List<Map<String, Object>> teacherData;

        if (shouldUseCache(cacheFile)) {
            teacherData = readFromCache(cacheFile);
        } else {
            teacherData = fetchDataFromMockaroo("teachers", count);
            saveToCache(teacherData, cacheFile);
        }

        List<User> teachers = new ArrayList<>();
        int batchSize = 20;

        for (int i = 0; i < Math.min(teacherData.size(), count); i += batchSize) {
            int batchEnd = Math.min(i + batchSize, Math.min(teacherData.size(), count));
            List<User> batch = new ArrayList<>();

            for (int j = i; j < batchEnd; j++) {
                Map<String, Object> data = teacherData.get(j);
                User teacher = User.builder()
                        .firstName((String) data.get("first_name"))
                        .lastName((String) data.get("last_name"))
                        .username("teacher" + (j + 1))
                        .email(data.get("email").toString())
                        .password(passwordEncoder.encode("password"))
                        .role("TEACHER")
                        .photoUrl(data.get("avatar").toString())
                        .teachingSubjects(new ArrayList<>())
                        .enabled(true)
                        .createdAt(new Date())
                        .updatedAt(new Date())
                        .build();

                batch.add(teacher);
            }

            teachers.addAll(userRepository.saveAll(batch));
            System.out.println("Lot de " + batch.size() + " enseignants créé (" + teachers.size() + "/" + count + ")");
        }

        return teachers;
    }

    private List<Subject> createSubjects(int count) throws IOException {
        System.out.println("Création de " + count + " matières...");

        String cacheFile = cacheDirectory + "/subjects.json";
        List<Map<String, Object>> subjectData;

        if (shouldUseCache(cacheFile)) {
            subjectData = readFromCache(cacheFile);
        } else {
            subjectData = fetchDataFromMockaroo("subjects", count);
            saveToCache(subjectData, cacheFile);
        }

        // Couleurs pour les matières
        String[] colors = {
                "#4CAF50", "#2196F3", "#F44336", "#9C27B0", "#FF9800",
                "#795548", "#607D8B", "#E91E63", "#9E9E9E", "#FFEB3B"
        };

        List<Subject> subjects = new ArrayList<>();
        Random random = new Random();
        int batchSize = 20;

        for (int i = 0; i < Math.min(subjectData.size(), count); i += batchSize) {
            int batchEnd = Math.min(i + batchSize, Math.min(subjectData.size(), count));
            List<Subject> batch = new ArrayList<>();

            for (int j = i; j < batchEnd; j++) {
                Map<String, Object> data = subjectData.get(j);

                Subject subject = Subject.builder()
                        .name((String) data.get("course_name"))
                        .imageUrl(data.get("image_url").toString())
                        .color(colors[random.nextInt(colors.length)])
                        .description((String) data.get("description"))
                        .createdAt(new Date())
                        .updatedAt(new Date())
                        .build();

                batch.add(subject);
            }

            subjects.addAll(subjectRepository.saveAll(batch));
            System.out.println("Lot de " + batch.size() + " matières créé (" + subjects.size() + "/" + count + ")");
        }

        return subjects;
    }

    private void updateTeachersWithSubjects(List<User> teachers, List<Subject> subjects) {
        System.out.println("Attribution des matières aux enseignants...");

        Random random = new Random();

        // Distribution des matières aux enseignants
        List<Subject> availableSubjects = new ArrayList<>(subjects);
        Collections.shuffle(availableSubjects);

        for (User teacher : teachers) {
            List<Subject> teacherSubjects = new ArrayList<>();

            // Attribution de 1 à 3 matières à chaque enseignant
            int numSubjectsToAssign = Math.min(random.nextInt(3) + 1, availableSubjects.size());

            for (int j = 0; j < numSubjectsToAssign; j++) {
                if (!availableSubjects.isEmpty()) {
                    Subject subject = availableSubjects.remove(0);
                    teacherSubjects.add(subject);
                }
            }

            // Si toutes les matières ont été assignées, recommencer la liste
            if (availableSubjects.isEmpty() && subjects.size() > 0) {
                availableSubjects = new ArrayList<>(subjects);
                Collections.shuffle(availableSubjects);
            }

            teacher.setTeachingSubjects(teacherSubjects);
        }

        userRepository.saveAll(teachers);
    }

    private List<User> createStudents(int count, List<Class> classes) throws IOException {
        System.out.println("Création de " + count + " étudiants...");

        String cacheFile = cacheDirectory + "/students.json";
        List<Map<String, Object>> studentData;

        if (shouldUseCache(cacheFile)) {
            studentData = readFromCache(cacheFile);
        } else {
            studentData = fetchDataFromMockaroo("students", count);
            saveToCache(studentData, cacheFile);
        }

        List<User> students = new ArrayList<>();
        Random random = new Random();
        int batchSize = 20;

        for (int i = 0; i < Math.min(studentData.size(), count); i += batchSize) {
            int batchEnd = Math.min(i + batchSize, Math.min(studentData.size(), count));
            List<User> batch = new ArrayList<>();

            for (int j = i; j < batchEnd; j++) {
                Map<String, Object> data = studentData.get(j);
                Class studentClass = classes.get(random.nextInt(classes.size()));

                User student = User.builder()
                        .firstName((String) data.get("first_name"))
                        .lastName((String) data.get("last_name"))
                        .username("student" + (j + 1))
                        .email(data.get("email").toString())
                        .password(passwordEncoder.encode("password"))
                        .role("STUDENT")
                        .photoUrl(data.get("avatar").toString())
                        .classId(studentClass)
                        .enabled(true)
                        .createdAt(new Date())
                        .updatedAt(new Date())
                        .build();

                batch.add(student);
            }

            students.addAll(userRepository.saveAll(batch));
            System.out.println("Lot de " + batch.size() + " étudiants créé (" + students.size() + "/" + count + ")");
        }

        return students;
    }

    private void updateClassesWithStudents(List<Class> classes, List<User> students) {
        System.out.println("Mise à jour des classes avec leurs étudiants...");

        // Grouper les étudiants par classe
        Map<String, List<User>> studentsByClass = students.stream()
                .filter(student -> student.getClassId() != null)
                .collect(Collectors.groupingBy(student -> student.getClassId().getId()));

        for (Class classEntity : classes) {
            List<User> classStudents = studentsByClass.get(classEntity.getId());
            if (classStudents != null) {
                classEntity.setStudents(classStudents);
            }
        }

        classRepository.saveAll(classes);
    }

    private void createAssignments(List<Subject> subjects, List<User> students, List<Class> classes, int count) throws IOException {
        System.out.println("Création de " + count + " devoirs...");

        String cacheFile = cacheDirectory + "/assignments.json";
        List<Map<String, Object>> assignmentData;

        if (shouldUseCache(cacheFile)) {
            assignmentData = readFromCache(cacheFile);
        } else {
            assignmentData = fetchDataFromMockaroo("assignments", count);
            saveToCache(assignmentData, cacheFile);
        }

        Random random = new Random();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        int batchSize = 20;

        for (int i = 0; i < Math.min(assignmentData.size(), count); i += batchSize) {
            int batchEnd = Math.min(i + batchSize, Math.min(assignmentData.size(), count));
            List<Assignment> batch = new ArrayList<>();

            for (int j = i; j < batchEnd; j++) {
                try {
                    Map<String, Object> data = assignmentData.get(j);

                    User student = students.get(random.nextInt(students.size()));
                    Subject subject = subjects.get(random.nextInt(subjects.size()));
                    Class classEntity = classes.get(random.nextInt(classes.size()));
                    boolean rendu = (boolean) data.get("rendu");

                    Date dateDeRendu = dateFormat.parse((String) data.get("date_de_rendu"));
                    Double note = null;
                    String remarques = null;

                    if (rendu) {
                        Object noteObj = data.get("note");
                        if (noteObj instanceof Number) {
                            note = ((Number) noteObj).doubleValue();
                        } else if (noteObj instanceof String) {
                            note = Double.parseDouble((String) noteObj);
                        }
                        remarques = (String) data.get("remarques");
                    }

                    Assignment assignment = Assignment.builder()
                            .nom((String) data.get("nom"))
                            .dateDeRendu(dateDeRendu)
                            .rendu(rendu)
                            .auteur(student)
                            .matiere(subject)
                            .classId(classEntity)
                            .note(note)
                            .remarques(remarques)
                            .attachments(new ArrayList<>())
                            .createdAt(new Date())
                            .updatedAt(new Date())
                            .build();

                    batch.add(assignment);
                } catch (Exception e) {
                    System.err.println("Erreur lors de la création d'un devoir : " + e.getMessage());
                }
            }

            assignmentRepository.saveAll(batch);
            System.out.println("Lot de " + batch.size() + " devoirs créé (" + (i + batch.size()) + "/" + count + ")");
        }
    }

    private List<Map<String, Object>> fetchDataFromMockaroo(String schemaName, int count) {
        System.out.println("Récupération de " + count + " données depuis Mockaroo pour le schéma '" + schemaName + "'...");

        // URLs Mockaroo pour chaque schéma
        Map<String, String> schemaIds = Map.of(
                "admins", "f72c1ec0",
                "teachers", "b8f9e030",
                "subjects", "1b344fb0",
                "students", "40b28830",
                "assignments", "9d0d6a70",
                "classes", "f38101c0"
        );

        String schemaId = schemaIds.get(schemaName);
        if (schemaId == null) {
            System.err.println("Schéma non trouvé: " + schemaName);
            return new ArrayList<>();
        }

        String url = "https://api.mockaroo.com/api/" + schemaId + "?count=" + count + "&key=" + mockarooApiKey;

        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            return response.getBody();
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des données depuis Mockaroo : " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private boolean shouldUseCache(String cacheFile) {
        if (!useDataCache) {
            return false;
        }
        Path path = Paths.get(cacheFile);
        return Files.exists(path);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> readFromCache(String cacheFile) throws IOException {
        System.out.println("Lecture des données depuis le cache : " + cacheFile);

        File file = new File(cacheFile);
        return objectMapper.readValue(file, List.class);
    }

    private void saveToCache(List<Map<String, Object>> data, String cacheFile) throws IOException {
        System.out.println("Sauvegarde des données dans le cache : " + cacheFile);

        File file = new File(cacheFile);
        objectMapper.writeValue(file, data);
    }
}