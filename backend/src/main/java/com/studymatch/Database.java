package com.studymatch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private final String url;

    public Database(String url) {
        this.url = url;
    }

    public Connection connect() throws SQLException {
        Connection connection = DriverManager.getConnection(url);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            connection.close();
            throw e;
        }
        return connection;
    }

    public void createTables() throws SQLException {
        try (Connection connection = connect();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS students (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        style TEXT,
                        goal TEXT
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS student_courses (
                        student_id INTEGER NOT NULL REFERENCES students(id),
                        course TEXT NOT NULL,
                        level TEXT NOT NULL,
                        exam_session TEXT,
                        PRIMARY KEY (student_id, course)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS student_slots (
                        student_id INTEGER NOT NULL REFERENCES students(id),
                        day INTEGER NOT NULL,
                        hour INTEGER NOT NULL,
                        PRIMARY KEY (student_id, day, hour)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS accounts (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        email TEXT NOT NULL UNIQUE,
                        password_hash TEXT NOT NULL,
                        verified INTEGER NOT NULL DEFAULT 0,
                        verification_token TEXT
                    )
                    """);
        }
    }
}