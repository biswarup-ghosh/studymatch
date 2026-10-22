package com.studymatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class ExamSessionTest {
    private static final Course CALCULUS = CourseCatalog.find("Calculus 1");

    private static Student student(String name, Level level) {
        Student s = new Student(name);
        s.setLevel(CALCULUS, level);
        s.getAvailability().add(0, 19);
        return s;
    }

    @Test
    void sameSessionAddsBonus() {
        Student me = student("Me", Level.NEED_HELP);
        Student anna = student("Anna", Level.STRONG);
        me.setExamSession(CALCULUS, ExamSession.SESSION_1);
        anna.setExamSession(CALCULUS, ExamSession.SESSION_1);

        List<MatchResult> result = MatchFinder.findTopMatches(me, List.of(me, anna), 5);

        assertEquals(6.5, result.get(0).getScore(), 0.0001);
    }

    @Test
    void differentSessionsGiveNoBonus() {
        Student me = student("Me", Level.NEED_HELP);
        Student anna = student("Anna", Level.STRONG);
        me.setExamSession(CALCULUS, ExamSession.SESSION_1);
        anna.setExamSession(CALCULUS, ExamSession.SESSION_2);

        List<MatchResult> result = MatchFinder.findTopMatches(me, List.of(me, anna), 5);

        assertEquals(4.5, result.get(0).getScore(), 0.0001);
    }

    @Test
    void blankSessionGivesNoBonus() {
        Student me = student("Me", Level.NEED_HELP);
        Student anna = student("Anna", Level.STRONG);
        me.setExamSession(CALCULUS, ExamSession.SESSION_1);

        List<MatchResult> result = MatchFinder.findTopMatches(me, List.of(me, anna), 5);

        assertEquals(4.5, result.get(0).getScore(), 0.0001);
    }

    @Test
    void sameSessionOutranksSameStyle() {
        Student me = student("Me", Level.NEED_HELP);
        me.setExamSession(CALCULUS, ExamSession.SESSION_1);
        me.setStyle(StudyStyle.QUIET);

        Student a = student("A", Level.STRONG);
        a.setExamSession(CALCULUS, ExamSession.SESSION_1);

        Student b = student("B", Level.STRONG);
        b.setStyle(StudyStyle.QUIET);

        List<MatchResult> result = MatchFinder.findTopMatches(me, List.of(me, b, a), 2);

        assertEquals("A", result.get(0).getStudent().getName());
        assertEquals(6.5, result.get(0).getScore(), 0.0001);
        assertEquals(5.5, result.get(1).getScore(), 0.0001);
    }

    @Test
    void explanationMentionsSharedSession() {
        Student me = student("Me", Level.NEED_HELP);
        Student anna = student("Anna", Level.STRONG);
        me.setExamSession(CALCULUS, ExamSession.SESSION_3);
        anna.setExamSession(CALCULUS, ExamSession.SESSION_3);

        List<MatchResult> result = MatchFinder.findTopMatches(me, List.of(me, anna), 5);

        assertTrue(MatchFinder.explain(me, result.get(0)).contains("same exam session"));
    }
}