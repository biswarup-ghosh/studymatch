package com.studymatch;

import java.util.HashMap;
import java.util.Map;

public class Student {
    private final String name;
    private final Map<Course, Level> levels = new HashMap<>();

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

    @Override
    public String toString() {
        return name + " " + levels;
    }
}