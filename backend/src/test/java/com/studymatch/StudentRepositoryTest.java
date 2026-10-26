package com.studymatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StudentRepositoryTest {
    @TempDir
    Path folder;

    private StudentRepository newRepository() throws SQLException {
        Database database = new Database("jdbc:sqlite:" + folder.resolve("test.db"));
        database.createTables();
        return new StudentRepository(database);
    }

    @Test
    void savedStudentComesBackIdentical() throws SQLException {
        StudentRepository repository = newRepository();
        Course calculus = CourseCatalog.find("Calculus 1");
        Student marco = new Student("Marco");
        marco.setLevel(calculus, Level.NEED_HELP);
        marco.setExamSession(calculus, ExamSession.SESSION_2);
        marco.getAvailability().add(0, 19);
        marco.getAvailability().add(3, 8);
        marco.setStyle(StudyStyle.QUIET);

        repository.save(marco);
        List<Student> loaded = repository.loadAll();

        assertEquals(1, loaded.size());
        Student copy = loaded.get(0);
        assertEquals("Marco", copy.getName());
        assertEquals(Level.NEED_HELP, copy.getLevel(calculus));
        assertEquals(ExamSession.SESSION_2, copy.getExamSession(calculus));
        assertTrue(copy.getAvailability().isFree(0, 19));
        assertTrue(copy.getAvailability().isFree(3, 8));
        assertEquals(2, copy.getAvailability().sharedHours(marco.getAvailability()));
        assertEquals(StudyStyle.QUIET, copy.getStyle());
        assertNull(copy.getGoal());
    }

    @Test
    void trickyNameIsStoredAsPlainText() throws SQLException {
        StudentRepository repository = newRepository();
        String nasty = "Robert'); DROP TABLE students;--";
        Student student = new Student(nasty);
        student.setLevel(CourseCatalog.find("Calculus 1"), Level.OKAY);
        student.getAvailability().add(0, 10);

        repository.save(student);
        List<Student> loaded = repository.loadAll();

        assertEquals(1, loaded.size());
        assertEquals(nasty, loaded.get(0).getName());
    }

    @Test
    void studentsComeBackInSavingOrder() throws SQLException {
        StudentRepository repository = newRepository();
        for (String name : List.of("A", "B", "C")) {
            Student student = new Student(name);
            student.setLevel(CourseCatalog.find("Calculus 1"), Level.OKAY);
            student.getAvailability().add(0, 10);
            repository.save(student);
        }

        List<Student> loaded = repository.loadAll();

        assertEquals(3, loaded.size());
        assertEquals("A", loaded.get(0).getName());
        assertEquals("B", loaded.get(1).getName());
        assertEquals("C", loaded.get(2).getName());
    }
}