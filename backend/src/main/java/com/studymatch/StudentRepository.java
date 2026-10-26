package com.studymatch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StudentRepository {
    private final Database database;

    public StudentRepository(Database database) {
        this.database = database;
    }

    public void save(Student student) throws SQLException {
        try (Connection connection = database.connect()) {
            connection.setAutoCommit(false);
            try {
                long id = insertStudent(connection, student);
                insertCourses(connection, id, student);
                insertSlots(connection, id, student);
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    public List<Student> loadAll() throws SQLException {
        Map<Long, Student> byId = new LinkedHashMap<>();
        try (Connection connection = database.connect();
             Statement statement = connection.createStatement()) {

            try (ResultSet rows = statement.executeQuery(
                    "SELECT id, name, style, goal FROM students ORDER BY id")) {
                while (rows.next()) {
                    Student student = new Student(rows.getString("name"));
                    String style = rows.getString("style");
                    if (style != null) {
                        student.setStyle(StudyStyle.valueOf(style));
                    }
                    String goal = rows.getString("goal");
                    if (goal != null) {
                        student.setGoal(StudyGoal.valueOf(goal));
                    }
                    byId.put(rows.getLong("id"), student);
                }
            }

            try (ResultSet rows = statement.executeQuery(
                    "SELECT student_id, course, level, exam_session FROM student_courses")) {
                while (rows.next()) {
                    Student student = byId.get(rows.getLong("student_id"));
                    String courseName = rows.getString("course");
                    try {
                        Course course = CourseCatalog.find(courseName);
                        student.setLevel(course, Level.valueOf(rows.getString("level")));
                        String session = rows.getString("exam_session");
                        if (session != null) {
                            student.setExamSession(course, ExamSession.valueOf(session));
                        }
                    } catch (IllegalArgumentException e) {
                        System.err.println("Skipping a saved course that no longer exists: " + courseName);
                    }
                }
            }

            try (ResultSet rows = statement.executeQuery(
                    "SELECT student_id, day, hour FROM student_slots")) {
                while (rows.next()) {
                    Student student = byId.get(rows.getLong("student_id"));
                    student.getAvailability().add(rows.getInt("day"), rows.getInt("hour"));
                }
            }
        }
        return new ArrayList<>(byId.values());
    }

    private long insertStudent(Connection connection, Student student) throws SQLException {
        String sql = "INSERT INTO students (name, style, goal) VALUES (?, ?, ?)";
        try (PreparedStatement statement =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, student.getName());
            statement.setString(2, nameOf(student.getStyle()));
            statement.setString(3, nameOf(student.getGoal()));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    private void insertCourses(Connection connection, long studentId, Student student) throws SQLException {
        String sql = "INSERT INTO student_courses (student_id, course, level, exam_session) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Map.Entry<Course, Level> entry : student.getLevels().entrySet()) {
                Course course = entry.getKey();
                statement.setLong(1, studentId);
                statement.setString(2, course.getName());
                statement.setString(3, entry.getValue().name());
                statement.setString(4, nameOf(student.getExamSession(course)));
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void insertSlots(Connection connection, long studentId, Student student) throws SQLException {
        String sql = "INSERT INTO student_slots (student_id, day, hour) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int day = 0; day < 7; day++) {
                for (int hour = Availability.FIRST_HOUR; hour < Availability.LAST_HOUR; hour++) {
                    if (student.getAvailability().isFree(day, hour)) {
                        statement.setLong(1, studentId);
                        statement.setInt(2, day);
                        statement.setInt(3, hour);
                        statement.addBatch();
                    }
                }
            }
            statement.executeBatch();
        }
    }

    private static String nameOf(Enum<?> value) {
        return value == null ? null : value.name();
    }
}