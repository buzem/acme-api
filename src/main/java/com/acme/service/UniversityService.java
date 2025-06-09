package com.acme.service;

import com.acme.dto.request.CreateLecturerRequest;
import com.acme.dto.request.CreateStudentRequest;
import com.acme.dto.response.LecturerResponse;
import com.acme.dto.response.StudentResponse;
import com.acme.entity.Lecturer;
import com.acme.entity.Student;
import com.acme.exception.LecturerAlreadyExistsException;
import com.acme.exception.LecturerNotFoundException;
import com.acme.exception.StudentAlreadyExistsException;
import com.acme.exception.StudentNotFoundException;
import com.acme.repository.LecturerRepository;
import com.acme.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.acme.exception.ErrorConstants.*;


@Service
@Transactional(readOnly = true)
public class UniversityService {

    private final LecturerRepository lecturerRepository;
    private final StudentRepository studentRepository;

    public UniversityService(LecturerRepository lecturerRepository, 
                           StudentRepository studentRepository) {
        this.lecturerRepository = lecturerRepository;
        this.studentRepository = studentRepository;
    }


    @Transactional
    public LecturerResponse createLecturer(CreateLecturerRequest request) {
        if (lecturerRepository.existsByLecturerId(request.lecturerId())) {
            throw new LecturerAlreadyExistsException(
                    formatLecturerAlreadyExists(request.lecturerId())
            );
        }

        Lecturer lecturer = new Lecturer(request.name(), request.surname(), request.lecturerId());
        return LecturerResponse.from(lecturerRepository.save(lecturer));
    }

    public LecturerResponse getLecturerById(String lecturerId) {
        Lecturer lecturer = lecturerRepository.findLecturerWithStudents(lecturerId)
                .orElseThrow(() -> new LecturerNotFoundException(formatLecturerNotFound(lecturerId)));
        
        return LecturerResponse.from(lecturer);
    }


    public StudentResponse getStudentById(String studentId) {
        Student student = studentRepository.findStudentWithLecturers(studentId)
                .orElseThrow(() -> new StudentNotFoundException(formatStudentNotFound(studentId)));
        
        return StudentResponse.from(student);
    }


    @Transactional
    public StudentResponse addStudentToLecturer(String lecturerId, CreateStudentRequest request) {
        Lecturer lecturer = lecturerRepository.findByLecturerId(lecturerId)
                .orElseThrow(() -> new LecturerNotFoundException(formatLecturerNotFound(lecturerId)));

        Student existingStudent = studentRepository.findByStudentId(request.studentId())
                .orElse(null);

        if (existingStudent != null) {
            validateStudentData(existingStudent, request);
            checkAssignmentConflict(existingStudent, lecturer, request.studentId());
            return assignExistingStudent(lecturer, existingStudent);
        } else {
            return createAndAssignNewStudent(lecturer, request);
        }
    }

    private void validateStudentData(Student existingStudent, CreateStudentRequest request) {
        if (!existingStudent.getName().equals(request.name()) || 
            !existingStudent.getSurname().equals(request.surname())) {
            throw new StudentAlreadyExistsException(
                    formatStudentIdConflict(request.studentId())
            );
        }
    }

    private void checkAssignmentConflict(Student student, Lecturer lecturer, String studentId) {
        if (student.getLecturers().contains(lecturer)) {
            throw new StudentAlreadyExistsException(
                    formatStudentAlreadyAssigned(studentId)
            );
        }
    }

    private StudentResponse assignExistingStudent(Lecturer lecturer, Student student) {
        lecturer.addStudent(student);
        return StudentResponse.from(student);
    }

    private StudentResponse createAndAssignNewStudent(Lecturer lecturer, CreateStudentRequest request) {
        Student student = new Student(request.name(), request.surname(), request.studentId());
        student = studentRepository.save(student);
        lecturer.addStudent(student);
        return StudentResponse.from(student);
    }
} 