package com.studymatch;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private static final int FAKE_STUDENTS = 1000;

    public static void main(String[] args) {
        Database database = new Database("jdbc:sqlite:studymatch.db");
        StudentRepository repository = new StudentRepository(database);
        AccountRepository accounts = new AccountRepository(database);

        List<Student> students = new CopyOnWriteArrayList<>(FakeData.generate(FAKE_STUDENTS, 42));
        Map<Long, Student> profileOf = new ConcurrentHashMap<>();
        try {
            database.createTables();
            List<StudentRepository.SavedStudent> saved = repository.loadAll();
            for (StudentRepository.SavedStudent entry : saved) {
                students.add(entry.student());
                profileOf.put(entry.accountId(), entry.student());
            }
            System.out.println("Loaded " + saved.size() + " saved profiles from the database");
        } catch (SQLException e) {
            throw new IllegalStateException("Could not open the database", e);
        }

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

        app.put("/api/profile", ctx -> {
            Long accountId = AccountRoutes.loggedInAccount(ctx);
            if (accountId == null) {
                ctx.status(401).json(Map.of("error", "please log in first"));
                return;
            }

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

            synchronized (students) {
                repository.saveFor(accountId, student);
                Student old = profileOf.put(accountId, student);
                if (old != null) {
                    students.remove(old);
                }
                students.add(student);
            }
            ctx.json(Map.of("name", student.getName()));
        });

        app.get("/api/profile", ctx -> {
            Long accountId = AccountRoutes.loggedInAccount(ctx);
            if (accountId == null) {
                ctx.status(401).json(Map.of("error", "please log in first"));
                return;
            }
            Student mine = profileOf.get(accountId);
            if (mine == null) {
                ctx.json(Map.of("exists", false));
            } else {
                ctx.json(profileJson(mine));
            }
        });

        app.get("/api/matches", ctx -> {
            Long accountId = AccountRoutes.loggedInAccount(ctx);
            if (accountId == null) {
                ctx.status(401).json(Map.of("error", "please log in first"));
                return;
            }
            Student me = profileOf.get(accountId);
            if (me == null) {
                ctx.status(404).json(Map.of("error", "create your profile first"));
                return;
            }
            ctx.json(matchesJson(me, students));
        });

        app.get("/api/matches/{id}", ctx -> {
            int id;
            try {
                id = Integer.parseInt(ctx.pathParam("id"));
            } catch (NumberFormatException e) {
                ctx.status(400).json(Map.of("error", "id must be a number"));
                return;
            }
            if (id < 0 || id >= FAKE_STUDENTS) {
                ctx.status(404).json(Map.of("error", "no such demo student"));
                return;
            }
            ctx.json(matchesJson(students.get(id), students));
        });

        app.start(7070);
    }

    private static Map<String, Object> matchesJson(Student me, List<Student> students) {
        List<Map<String, Object>> matches = new ArrayList<>();
        for (MatchResult result : MatchFinder.findTopMatches(me, students, 5)) {
            matches.add(Map.of(
                    "name", result.getStudent().getName(),
                    "score", result.getScore(),
                    "sharedHours", result.getSharedHours(),
                    "why", MatchFinder.explain(me, result)));
        }
        return Map.of("student", me.getName(), "matches", matches);
    }

    private static Map<String, Object> profileJson(Student student) {
        List<Map<String, Object>> courses = new ArrayList<>();
        for (Map.Entry<Course, Level> entry : student.getLevels().entrySet()) {
            Course course = entry.getKey();
            ExamSession session = student.getExamSession(course);
            Map<String, Object> item = new HashMap<>();
            item.put("course", course.getName());
            item.put("level", entry.getValue().name());
            item.put("session", session == null ? null : session.name());
            courses.add(item);
        }

        List<Map<String, Object>> slots = new ArrayList<>();
        for (int day = 0; day < 7; day++) {
            for (int hour = Availability.FIRST_HOUR; hour < Availability.LAST_HOUR; hour++) {
                if (student.getAvailability().isFree(day, hour)) {
                    slots.add(Map.of("day", day, "hour", hour));
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("exists", true);
        result.put("name", student.getName());
        result.put("courses", courses);
        result.put("availability", slots);
        result.put("style", student.getStyle() == null ? null : student.getStyle().name());
        result.put("goal", student.getGoal() == null ? null : student.getGoal().name());
        return result;
    }
}