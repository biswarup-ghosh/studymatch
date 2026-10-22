package com.studymatch;

import java.util.HashMap;
import java.util.Map;

public class Student {
    private final String name;
    private final Map<Course, Level> levels = new HashMap<>();
    private final Map<Course, ExamSession> examSessions = new HashMap<>();
    private final Availability availability = new Availability();
    private StudyStyle style;
    private StudyGoal goal;

    public Student(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setLevel(Course course, Level level) {
        levels.put(course, level);
    }

    public Level getLevel(Course course) {
        return levels.get(course);
    }

    public Map<Course, Level> getLevels() {
        return Map.copyOf(levels);
    }

    public void setExamSession(Course course, ExamSession session) {
        examSessions.put(course, session);
    }

    public ExamSession getExamSession(Course course) {
        return examSessions.get(course);
    }

    public Availability getAvailability() {
        return availability;
    }

    public StudyStyle getStyle() {
        return style;
    }

    public void setStyle(StudyStyle style) {
        this.style = style;
    }

    public StudyGoal getGoal() {
        return goal;
    }

    public void setGoal(StudyGoal goal) {
        this.goal = goal;
    }

    @Override
    public String toString() {
        return name + " " + levels;
    }
}