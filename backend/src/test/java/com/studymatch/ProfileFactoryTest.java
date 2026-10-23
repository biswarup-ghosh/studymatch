package com.studymatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class ProfileFactoryTest {
    private static final Course CALCULUS = CourseCatalog.find("Calculus 1");

    private static ProfileRequest request(String name, String course, String level, int hour) {
        return new ProfileRequest(
                name,
                List.of(new ProfileRequest.CourseEntry(course, level, null)),
                List.of(new ProfileRequest.SlotEntry(0, hour)),
                null,
                null);
    }

    @Test
    void buildsStudentFromValidRequest() {
        ProfileRequest request = new ProfileRequest(
                "  Marco  ",
                List.of(new ProfileRequest.CourseEntry("Calculus 1", "NEED_HELP", "SESSION_2")),
                List.of(new ProfileRequest.SlotEntry(0, 19)),
                "QUIET",
                "PASS");

        Student student = ProfileFactory.build(request);

        assertEquals("Marco", student.getName());
        assertEquals(Level.NEED_HELP, student.getLevel(CALCULUS));
        assertEquals(ExamSession.SESSION_2, student.getExamSession(CALCULUS));
        assertTrue(student.getAvailability().isFree(0, 19));
        assertEquals(StudyStyle.QUIET, student.getStyle());
        assertEquals(StudyGoal.PASS, student.getGoal());
    }

    @Test
    void optionalFieldsCanBeBlank() {
        Student student = ProfileFactory.build(request("Anna", "Calculus 1", "STRONG", 10));

        assertNull(student.getStyle());
        assertNull(student.getGoal());
        assertNull(student.getExamSession(CALCULUS));
    }

    @Test
    void rejectsEmptyName() {
        assertThrows(IllegalArgumentException.class,
                () -> ProfileFactory.build(request("   ", "Calculus 1", "STRONG", 10)));
    }

    @Test
    void rejectsUnknownCourse() {
        assertThrows(IllegalArgumentException.class,
                () -> ProfileFactory.build(request("Luca", "Basket Weaving", "STRONG", 10)));
    }

    @Test
    void rejectsUnknownLevel() {
        assertThrows(IllegalArgumentException.class,
                () -> ProfileFactory.build(request("Luca", "Calculus 1", "GENIUS", 10)));
    }

    @Test
    void rejectsHourOutsideTheRange() {
        assertThrows(IllegalArgumentException.class,
                () -> ProifleFactory.build(request("Luca", "Calculus 1", "STRONG", 3)));
    }
}