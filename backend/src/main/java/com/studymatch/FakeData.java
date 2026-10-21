package com.studymatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FakeData {
    public static List<Student> generate(int count, long seed) {
        Random random = new Random(seed);
        Level[] levels = Level.values();
        List<Course> courses = CourseCatalog.ALL;
        List<Student> students = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Student student = new Student("Student" + i);

            int numberOfCourses = 3 + random.nextInt(3);
            for (int c = 0; c < numberOfCourses; c++) {
                Course course = courses.get(random.nextInt(courses.size()));
                Level level = levels[random.nextInt(levels.length)];
                student.setLevel(course, level);
            }

            int numberOfSlots = 5 + random.nextInt(16);
            for (int s = 0; s < numberOfSlots; s++) {
                int day = random.nextInt(7);
                int hours = Availability.LAST_HOUR - Availability.FIRST_HOUR;
                int hour = Availability.FIRST_HOUR + random.nextInt(hours);
                student.getAvailability().add(day, hour);
            }

            students.add(student);
        }
        return students;
    }
}