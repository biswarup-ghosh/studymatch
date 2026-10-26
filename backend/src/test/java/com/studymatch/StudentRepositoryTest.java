package com.studymatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StudentRepositoryTest {
    @TempDir
    Path folder;

    private AccountRepository accounts;
    private StudentRepository repository;

    @BeforeEach
    void setUp() throws SQLException {
        Database database = new Database("jdbc:sqlite:" + folder.resolve("test.db"));
        database.createTables();
        accounts = new AccountRepository(database);
        repository = new StudentRepository(database);
    }

    private long newAccount(String email) throws SQLException {
        accounts.create(email, "hash", "token-" + email);
        return accounts.findByEmail(email).id();
    }

    private static Student simpleStudent(String name, Level level, int day, int hour) {
        Student student = new Student(name);
        student.setLevel(CourseCatalog.find("Calculus 1"), level);
        student.getAvailability().add(day, hour);
        return student;
    }

    @Test
    void savedStudentComesBackIdentical() throws SQLException {
        long account = newAccount("marco@studenti.unipd.it");
        Course calculus = CourseCatalog.find("Calculus 1");
        Student marco = new Student("Marco");
        marco.setLevel(calculus, Level.NEED_HELP);
        marco.setExamSession(calculus, ExamSession.SESSION_2);
        marco.getAvailability().add(0, 19);
        marco.getAvailability().add(3, 8);
        marco.setStyle(StudyStyle.QUIET);

        repository.saveFor(account, marco);
        List<StudentRepository.SavedStudent> loaded = repository.loadAll();

        assertEquals(1, loaded.size());
        assertEquals(account, loaded.get(0).accountId());
        Student copy = loaded.get(0).student();
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
        long account = newAccount("robert@studenti.unipd.it");
        String nasty = "Robert'); DROP TABLE students;--";

        repository.saveFor(account, simpleStudent(nasty, Level.OKAY, 0, 10));
        List<StudentRepository.SavedStudent> loaded = repository.loadAll();

        assertEquals(1, loaded.size());
        assertEquals(nasty, loaded.get(0).student().getName());
    }

    @Test
    void studentsComeBackInSavingOrder() throws SQLException {
        for (String name : List.of("A", "B", "C")) {
            long account = newAccount(name.toLowerCase() + "@studenti.unipd.it");
            repository.saveFor(account, simpleStudent(name, Level.OKAY, 0, 10));
        }

        List<StudentRepository.SavedStudent> loaded = repository.loadAll();

        assertEquals(3, loaded.size());
        assertEquals("A", loaded.get(0).student().getName());
        assertEquals("B", loaded.get(1).student().getName());
        assertEquals("C", loaded.get(2).student().getName());
    }

    @Test
    void savingAgainReplacesTheOldProfile() throws SQLException {
        long account = newAccount("marco@studenti.unipd.it");
        repository.saveFor(account, simpleStudent("Old name", Level.OKAY, 0, 10));

        repository.saveFor(account, simpleStudent("New name", Level.STRONG, 1, 9));
        List<StudentRepository.SavedStudent> loaded = repository.loadAll();

        assertEquals(1, loaded.size());
        Student copy = loaded.get(0).student();
        assertEquals("New name", copy.getName());
        assertEquals(Level.STRONG, copy.getLevel(CourseCatalog.find("Calculus 1")));
        assertFalse(copy.getAvailability().isFree(0, 10));
        assertTrue(copy.getAvailability().isFree(1, 9));
    }

    @Test
    void profileForUnknownAccountIsRejectedAndLeavesNothingBehind() throws SQLException {
        Student student = simpleStudent("Ghost", Level.OKAY, 0, 10);

        assertThrows(SQLException.class, () -> repository.saveFor(999, student));

        assertTrue(repository.loadAll().isEmpty());
    }
}