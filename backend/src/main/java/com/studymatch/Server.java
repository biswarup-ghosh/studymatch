package com.studymatch;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private static final int FAKE_STUDENTS = 1000;

    public static void main(String[] args) {
        Database database = new Database("jdbc:sqlite:studymatch.db");
        StudentRepository repository = new StudentRepository(database);

        List<Student> students = new CopyOnWriteArrayList<>(FakeData.generate(FAKE_STUDENTS, 42));
        try {
            database.createTables();
            List<Student> saved = repository.loadAll();
            students.addAll(saved);
            System.out.println("Loaded " + saved.size() + " saved students from the database");
        } catch (SQLException e) {
            throw new IllegalStateException("Could not open the database", e);
        }

        AccountRepository accounts = new AccountRepository(database);

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public", Location.CLASSPATH);
            config.jetty.modifyServletContextHandler(handler ->
                    handler.getSessionHandler().setHttpOnly(true));
        });

        AccountRoutes.register(app, accounts);

        app.get("/api/health", ctx -> ctx.json(Map.of("status", "ok")));

        app.get("/api/courses", ctx -> {
            List<Map<String, Object>> courses = new ArrayList<>();
            for (Course course : CourseCatalog.ALL) {
                courses.add(Map.of(
                        "name", course.getName(),
                        "year", course.getYear(),
                        "department", course.getDepartment()));
            }
            ctx.json(courses);
        });

        app.post("/api/students", ctx -> {
            Student student;
            try {
                ProfileRequest request = ctx.bodyAsClass(ProfileRequest.class);
                student = ProfileFactory.build(request);
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
                return;
            } catch (Exception e) {
                ctx.status(400).json(Map.of("error", "invalid request body"));
                return;
            }

            int id;
            try {
                synchronized (students) {
                    repository.save(student);
                    students.add(student);
                    id = students.size() - 1;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                ctx.status(500).json(Map.of("error", "could not save your profile, please try again"));
                return;
            }
            ctx.status(201).json(Map.of("id", id, "name", student.getName()));
        });

        app.get("/api/matches/{id}", ctx -> {
            int id;
            try {
                id = Integer.parseInt(ctx.pathParam("id"));
            } catch (NumberFormatException e) {
                ctx.status(400).json(Map.of("error", "id must be a number"));
                return;
            }
            if (id < 0 || id >= students.size()) {
                ctx.status(404).json(Map.of("error", "no such student"));
                return;
            }

            Student me = students.get(id);
            List<Map<String, Object>> matches = new ArrayList<>();
            for (MatchResult result : MatchFinder.findTopMatches(me, students, 5)) {
                matches.add(Map.of(
                        "name", result.getStudent().getName(),
                        "score", result.getScore(),
                        "sharedHours", result.getSharedHours(),
                        "why", MatchFinder.explain(me, result)));
            }
            ctx.json(Map.of("student", me.getName(), "matches", matches));
        });

        app.start(7070);
    }
}