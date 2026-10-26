package com.studymatch;

import java.util.Locale;
import java.util.regex.Pattern;

public class AccountRules {
    public static final String ALLOWED_DOMAIN = "studenti.unipd.it";
    public static final int MIN_PASSWORD = 10;
    public static final int MAX_PASSWORD = 200;
    private static final Pattern LOCAL_PART = Pattern.compile("[a-z0-9._+-]{1,64}");

    public static String normalizeEmail(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("Email is required");
        }
        String email = raw.trim().toLowerCase(Locale.ROOT);
        int at = email.lastIndexOf('@');
        String local = at < 0 ? "" : email.substring(0, at);
        String domain = at < 0 ? "" : email.substring(at + 1);
        if (!domain.equals(ALLOWED_DOMAIN) || !LOCAL_PART.matcher(local).matches()) {
            throw new IllegalArgumentException("Use your university email, ending in @" + ALLOWED_DOMAIN);
        }
        return email;
    }

    public static void checkPassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD || password.length() > MAX_PASSWORD) {
            throw new IllegalArgumentException(
                    "Password must be " + MIN_PASSWORD + " to " + MAX_PASSWORD + " characters");
        }
    }
}