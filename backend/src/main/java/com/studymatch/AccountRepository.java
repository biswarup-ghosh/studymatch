package com.studymatch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountRepository {
    public record Account(long id, String email, String passwordHash, boolean verified) {
    }

    private final Database database;

    public AccountRepository(Database database) {
        this.database = database;
    }

    public boolean create(String email, String passwordHash, String token) throws SQLException {
        String sql = "INSERT INTO accounts (email, password_hash, verification_token) VALUES (?, ?, ?)";
        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            statement.setString(2, passwordHash);
            statement.setString(3, token);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
                return false;
            }
            throw e;
        }
    }

    public Account findByEmail(String email) throws SQLException {
        return findOne("SELECT id, email, password_hash, verified FROM accounts WHERE email = ?", email);
    }

    public Account findById(long id) throws SQLException {
        return findOne("SELECT id, email, password_hash, verified FROM accounts WHERE id = ?", id);
    }

    public boolean verify(String token) throws SQLException {
        String sql = "UPDATE accounts SET verified = 1, verification_token = NULL WHERE verification_token = ?";
        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, token);
            return statement.executeUpdate() == 1;
        }
    }

    private Account findOne(String sql, Object parameter) throws SQLException {
        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, parameter);
            try (ResultSet row = statement.executeQuery()) {
                if (!row.next()) {
                    return null;
                }
                return new Account(
                        row.getLong("id"),
                        row.getString("email"),
                        row.getString("password_hash"),
                        row.getInt("verified") == 1);
            }
        }
    }
}