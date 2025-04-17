package com.esatic.assignmentapp.service;

import com.esatic.assignmentapp.exception.ResourceNotFoundException;
import com.esatic.assignmentapp.model.Teacher;
import com.esatic.assignmentapp.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;

    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }

    public Teacher getTeacherById(String id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));
    }

    public Teacher createTeacher(Teacher teacher) {
        teacher.setCreatedAt(new Date());
        teacher.setUpdatedAt(new Date());
        return teacherRepository.save(teacher);
    }

    public Teacher updateTeacher(String id, Teacher teacherDetails) {
        Teacher teacher = getTeacherById(id);

        teacher.setFirstName(teacherDetails.getFirstName());
        teacher.setLastName(teacherDetails.getLastName());
        teacher.setEmail(teacherDetails.getEmail());
        teacher.setPhotoUrl(teacherDetails.getPhotoUrl());
        teacher.setSubjects(teacherDetails.getSubjects());
        teacher.setUpdatedAt(new Date());

        return teacherRepository.save(teacher);
    }

    public void deleteTeacher(String id) {
        Teacher teacher = getTeacherById(id);
        teacherRepository.delete(teacher);
    }
}
