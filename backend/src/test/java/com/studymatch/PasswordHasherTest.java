package com.studymatch;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {
    @Test
    void correctPasswordMatches() {
        String stored = PasswordHasher.hash("correct horse battery");

        assertTrue(PasswordHasher.matches("correct horse battery", stored));
    }

    @Test
    void wrongPasswordDoesNotMatch() {
        String stored = PasswordHasher.hash("correct horse battery");

        assertFalse(PasswordHasher.matches("correct horse batterz", stored));
    }

    @Test
    void sameePasswordGivesDifferentHashesAndNeverStoresItself() {
        String first = PasswordHasher.hash("correct horse battery");
        String second = PasswordHasher.hash("correct horse battery");

        assertNotEquals(first, second);
        assertFalse(first.contains("correct horse battery"));
    }

    @Test
    void garbageStoredValueNeverMatches() {
        assertFalse(PasswordHasher.matches("anything", "not a hash"));
        assertFalse(PasswordHasher.matches("anything", "1:%%%:%%%"));
    }
}