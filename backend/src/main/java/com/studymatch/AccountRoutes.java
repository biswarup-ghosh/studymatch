package com.studymatch;

import io.javalin.Javalin;
import io.javalin.http.Context;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;

public class AccountRoutes {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String DUMMY_HASH = PasswordHasher.hash("not-a-real-password");
    private static final Map<String, String> WRONG_LOGIN = Map.of("error", "wrong email or password");

    public record Credentials(String email, String password) {
    }

    public static Long loggedInAccount(Context ctx) {
        return ctx.sessionAttribute("accountId");
    }

    public static void register(Javalin app, AccountRepository accounts) {
        app.exception(SQLException.class, (e, ctx) -> {
            e.printStackTrace();
            ctx.status(500).json(Map.of("error", "server error, please try again"));
        });

        app.post("/api/register", ctx -> {
            Credentials credentials;
            String email;
            try {
                credentials = ctx.bodyAsClass(Credentials.class);
                if (credentials == null) {
                    throw new IllegalStateException("empty body");
                }
                email = AccountRules.normalizeEmail(credentials.email());
                AccountRules.checkPassword(credentials.password());
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
                return;
            } catch (Exception e) {
                ctx.status(400).json(Map.of("error", "invalid request body"));
                return;
            }

            String token = newToken();
            boolean created = accounts.create(email, PasswordHasher.hash(credentials.password()), token);
            if (!created) {
                ctx.status(409).json(Map.of("error", "an account with this email already exists"));
                return;
            }
            System.out.println("VERIFICATION LINK (development only): http://localhost:7070/api/verify?token=" + token);
            ctx.status(201).json(Map.of("message", "Account created. Open the verification link to activate it."));
        });

        app.get("/api/verify", ctx -> {
            String token = ctx.queryParam("token");
            boolean ok = token != null && !token.isBlank() && accounts.verify(token);
            ctx.redirect(ok ? "/?verified=1" : "/?verified=0");
        });

        app.post("/api/login", ctx -> {
            Credentials credentials;
            try {
                credentials = ctx.bodyAsClass(Credentials.class);
                if (credentials == null) {
                    throw new IllegalStateException("empty body");
                }
            } catch (Exception e) {
                ctx.status(400).json(Map.of("error", "invalid request body"));
                return;
            }

            String email = credentials.email() == null ? "" : credentials.email().trim().toLowerCase(Locale.ROOT);
            String password = credentials.password() == null ? "" : credentials.password();
            if (password.length() > AccountRules.MAX_PASSWORD) {
                ctx.status(401).json(WRONG_LOGIN);
                return;
            }

            AccountRepository.Account account = accounts.findByEmail(email);
            String storedHash = account == null ? DUMMY_HASH : account.passwordHash();
            boolean passwordOk = PasswordHasher.matches(password, storedHash);
            if (account == null || !passwordOk) {
                ctx.status(401).json(WRONG_LOGIN);
                return;
            }
            if (!account.verified()) {
                ctx.status(403).json(Map.of("error", "please verify your email first"));
                return;
            }

            ctx.req().getSession().invalidate();
            ctx.sessionAttribute("accountId", account.id());
            ctx.json(Map.of("email", account.email()));
        });

        app.get("/api/me", ctx -> {
            Long id = ctx.sessionAttribute("accountId");
            AccountRepository.Account account = id == null ? null : accounts.findById(id);
            if (account == null) {
                ctx.status(401).json(Map.of("error", "not logged in"));
                return;
            }
            ctx.json(Map.of("email", account.email()));
        });

        app.post("/api/logout", ctx -> {
            ctx.req().getSession().invalidate();
            ctx.json(Map.of("status", "logged out"));
        });
    }

    private static String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}