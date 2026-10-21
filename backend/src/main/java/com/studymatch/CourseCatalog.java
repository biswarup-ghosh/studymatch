package com.studymatch;

import java.util.List;

public class CourseCatalog {
    private static final String DEPARTMENT = "Information Engineering";

    public static final List<Course> ALL = List.of(
        new Course("Calculus 1", 1, DEPARTMENT),
        new Course("Foundations of Computer Science", 1, DEPARTMENT),
        new Course("Linear Algebra", 1, DEPARTMENT),
        new Course("Physics 1", 1, DEPARTMENT),
        new Course("Digital Systems", 1, DEPARTMENT),
        new Course("Calculus 2", 2, DEPARTMENT),
        new Course("Physics 2", 2, DEPARTMENT),
        new Course("Data Structures and Algorithms", 2, DEPARTMENT)
    );

    public static Course find(String name) {
        for (Course course : ALL) {
            if (course.getName().equals(name)) {
                return course;
            }
        }
        throw new IllegalArgumentException("Unknown course: " + name);
    }
}