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
    private static final double POINTS_PER_SHARED_HOUR = 0.5;

    public static List<MatchResult> findTopMatches(Student me, Iterable<Student> everyone, int k) {
        PriorityQueue<MatchResult> best = new PriorityQueue<>();

        for (Student other : everyone) {
            if (other == me) {
                continue;
            }
            MatchResult result = score(me, other);
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

    private static MatchResult score(Student me, Student other) {
        double courseScore = 0;
        for (Map.Entry<Course, Level> entry : me.getLevels().entrySet()) {
            Course course = entry.getKey();
            Level myLevel = entry.getValue();
            Level theirLevel = other.getLevel(course);
            if (theirLevel == null) {
                continue;
            }
            courseScore += pointsFor(myLevel, theirLevel);
        }
        if (courseScore == 0) {
            return null;
        }

        int hours = me.getAvailability().sharedHours(other.getAvailability());
        if (hours == 0) {
            return null;
        }

        double total = courseScore + hours * POINTS_PER_SHARED_HOUR;
        return new MatchResult(other, total, hours);
    }

    private static double pointsFor(Level a, Level b) {
        boolean complementary = (a == Level.NEED_HELP && b == Level.STRONG)
                || (a == Level.STRONG && b == Level.NEED_HELP);
        if (complementary) {
            return COMPLEMENTARY_POINTS;
        }
        if (a == Level.NEED_HELP && b == Level.NEED_HELP) {
            return BOTH_NEED_HELP_POINTS;
        }
        return SHARED_COURSE_POINTS;
    }
}