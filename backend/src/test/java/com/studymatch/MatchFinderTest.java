package com.studymatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class MatchFinderTest {
    private static final Course CALCULUS = CourseCatalog.find("Calculus 1");

    private static Student student(String name, Level level, int day, int hour) {
        Student s = new Student(name);
        s.setLevel(CALCULUS, level);
        s.getAvailability().add(day, hour);
        return s;
    }

    @Test
    void complementaryPairWithSharedHourScoresAsExpected() {
        Student me = student("Me", Level.NEED_HELP, 0, 19);
        Student anna = student("Anna", Level.STRONG, 0, 19);

        List<MatchResult> result = MatchFinder.findTopMatches(me, List.of(me, anna), 5);

        assertEquals(1, result.size());
        assertEquals(4.5, result.get(0).getScore(), 0.0001);
    }

    @Test
    void noSharedCourseMeansNoMatch() {
        Student me = student("Me", Level.NEED_HELP, 0, 19);
        Student sara = new Student("Sara");
        sara.setLevel(CourseCatalog.find("Physics 1"), Level.STRONG);
        sara.getAvailability().add(0, 19);

        List<MatchResult> result = MatchFinder.findTopMatches(me, List.of(me, sara), 5);

        assertTrue(result.isEmpty());
    }

    @Test
    void noSharedHoursMeansNoMatch() {
        Student me = student("Me", Level.NEED_HELP, 0, 19);
        Student luca = student("Luca", Level.STRONG, 1, 10);

        List<MatchResult> result = MatchFinder.findTopMatches(me, List.of(me, luca), 5);

        assertTrue(result.isEmpty());
    }

    @Test
    void returnsOnlyTheBestKInOrder() {
        Student me = student("Me", Level.NEED_HELP, 0, 19);
        Student a = student("A", Level.STRONG, 0, 19);
        Student b = student("B", Level.NEED_HELP, 0, 19);
        Student c = student("C", Level.OKAY, 0, 19);

        List<MatchResult> result = MatchFinder.findTopMatches(me, List.of(me, c, b, a), 2);

        assertEquals(2, result.size());
        assertEquals("A", result.get(0).getStudent().getName());
        assertEquals("B", result.get(1).getStudent().getName());
    }
        @Test
    void sameStyleAndGoalAddHabitBonus() {
        Student me = student("Me", Level.NEED_HELP, 0, 19);
        Student anna = student("Anna", Level.STRONG, 0, 19);
        me.setStyle(StudyStyle.QUIET);
        anna.setStyle(StudyStyle.QUIET);
        me.setGoal(StudyGoal.PASS);
        anna.setGoal(StudyGoal.PASS);

        List<MatchResult> result = MatchFinder.findTopMatches(me, List.of(me, anna), 5);

        assertEquals(5.5, result.get(0).getScore(), 0.0001);
    }

    @Test
    void blankHabitsGiveNoBonus() {
        Student me = student("Me", Level.NEED_HELP, 0, 19);
        Student anna = student("Anna", Level.STRONG, 0, 19);
        me.setStyle(StudyStyle.QUIET);
        me.setGoal(StudyGoal.PASS);

        List<MatchResult> result = MatchFinder.findTopMatches(me, List.of(me, anna), 5);

        assertEquals(4.5, result.get(0).getScore(), 0.0001);
    }
}