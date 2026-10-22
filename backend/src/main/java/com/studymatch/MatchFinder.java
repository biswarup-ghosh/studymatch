package com.studymatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class MatchFinder {
    private static final double COMPLEMENTARY_POINTS = 3.0;
    private static final double BOTH_NEED_HELP_POINTS = 2.0;
    private static final double SHARED_COURSE_POINTS = 1.0;
    private static final double SAME_EXAM_SESSION_POINTS = 2.0;
    private static final double POINTS_PER_SHARED_HOUR = 0.5;
    private static final double POINTS_PER_RUN_HOUR = 1.0;
    private static final double SAME_STYLE_POINTS = 1.0;
    private static final double SAME_GOAL_POINTS = 0.5;

    public static List<MatchResult> findTopMatches(Student me, Iterable<Student> everyone, int k) {
        PriorityQueue<MatchResult> best = new PriorityQueue<>();
        Map<Course, Level> myLevels = me.getLevels();

        for (Student other : everyone) {
            if (other == me) {
                continue;
            }
            MatchResult result = score(me, myLevels, other);
            if (result == null) {
                continue;
            }
            if (best.size() < k) {
                best.add(result);
            } else if (result.compareTo(best.peek()) > 0) {
                best.poll();
                best.add(result);
            }
        }

        List<MatchResult> sorted = new ArrayList<>(best);
        Collections.sort(sorted, Collections.reverseOrder());
        return sorted;
    }

    public static String explain(Student me, MatchResult result) {
        Student other = result.getStudent();
        List<String> parts = new ArrayList<>();

        List<String> sharedCourses = new ArrayList<>();
        for (Map.Entry<Course, Level> entry : me.getLevels().entrySet()) {
            Course course = entry.getKey();
            Level theirLevel = other.getLevel(course);
            if (theirLevel != null) {
                String detail = describe(entry.getValue(), theirLevel);
                if (sameExamSession(me, other, course)) {
                    detail += ", same exam session";
                }
                sharedCourses.add(course.getName() + " (" + detail + ")");
            }
        }
        Collections.sort(sharedCourses);
        parts.add("shared courses: " + String.join(", ", sharedCourses));

        int hours = me.getAvailability().sharedHours(other.getAvailability());
        int run = me.getAvailability().longestSharedRun(other.getAvailability());
        parts.add("free together: " + hours + "h per week (longest block " + run + "h)");

        if (sameStyle(me, other)) {
            parts.add("same study style");
        }
        if (sameGoal(me, other)) {
            parts.add("same study goal");
        }
        return String.join("; ", parts);
    }

    private static MatchResult score(Student me, Map<Course, Level> myLevels, Student other) {
        double courseScore = 0;
        double examScore = 0;
        for (Map.Entry<Course, Level> entry : myLevels.entrySet()) {
            Course course = entry.getKey();
            Level myLevel = entry.getValue();
            Level theirLevel = other.getLevel(course);
            if (theirLevel == null) {
                continue;
            }
            courseScore += pointsFor(myLevel, theirLevel);
            if (sameExamSession(me, other, course)) {
                examScore += SAME_EXAM_SESSION_POINTS;
            }
        }
        if (courseScore == 0) {
            return null;
        }

        int hours = me.getAvailability().sharedHours(other.getAvailability());
        if (hours == 0) {
            return null;
        }
        int run = me.getAvailability().longestSharedRun(other.getAvailability());

        double total = courseScore
                + examScore
                + hours * POINTS_PER_SHARED_HOUR
                + run * POINTS_PER_RUN_HOUR
                + habitPoints(me, other);
        return new MatchResult(other, total, hours);
    }

    private static boolean sameExamSession(Student me, Student other, Course course) {
        ExamSession mine = me.getExamSession(course);
        return mine != null && mine == other.getExamSession(course);
    }

    private static boolean sameStyle(Student me, Student other) {
        return me.getStyle() != null && me.getStyle() == other.getStyle();
    }

    private static boolean sameGoal(Student me, Student other) {
        return me.getGoal() != null && me.getGoal() == other.getGoal();
    }

    private static double habitPoints(Student me, Student other) {
        double points = 0;
        if (sameStyle(me, other)) {
            points += SAME_STYLE_POINTS;
        }
        if (sameGoal(me, other)) {
            points += SAME_GOAL_POINTS;
        }
        return points;
    }

    private static boolean isComplementary(Level a, Level b) {
        return (a == Level.NEED_HELP && b == Level.STRONG)
                || (a == Level.STRONG && b == Level.NEED_HELP);
    }

    private static double pointsFor(Level a, Level b) {
        if (isComplementary(a, b)) {
            return COMPLEMENTARY_POINTS;
        }
        if (a == Level.NEED_HELP && b == Level.NEED_HELP) {
            return BOTH_NEED_HELP_POINTS;
        }
        return SHARED_COURSE_POINTS;
    }

    private static String describe(Level mine, Level theirs) {
        if (isComplementary(mine, theirs)) {
            return mine == Level.NEED_HELP ? "they can help you" : "you can help them";
        }
        if (mine == Level.NEED_HELP && theirs == Level.NEED_HELP) {
            return "you both need help";
        }
        return "you both take it";
    }
}