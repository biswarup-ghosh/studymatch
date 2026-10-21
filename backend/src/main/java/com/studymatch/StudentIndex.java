package com.studymatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StudentIndex {
    private final Map<Course, List<Student>> byCourse = new HashMap<>();

    public StudentIndex(List<Student> students) {
        for (Student student : students) {
            for (Course course : student.getLevels().keySet()) {
                byCourse.computeIfAbsent(course, c -> new ArrayList<>()).add(student);
            }
        }
    }

    public Set<Student> candidatesFor(Student me) {
        Set<Student> candidates = new HashSet<>();
        for (Course course : me.getLevels().keySet()) {
            List<Student> inCourse = byCourse.get(course);
            if (inCourse != null) {
                candidates.addAll(inCourse);
            }
        }
        candidates.remove(me);
        return candidates;
    }
}