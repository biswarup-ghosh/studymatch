package com.studymatch;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AccountRulesTest {
    @Test
    void cleansUpCaseAndSpaces() {
        assertEquals("marco.rossi@studenti.unipd.it",
                AccountRules.normalizeEmail("  Marco.Rossi@Studenti.UniPD.it "));
    }

    @Test
    void rejectsOtherDomains() {
        assertThrows(IllegalArgumentException.class, () -> AccountRules.normalizeEmail("marco@gmail.com"));
    }

    @Test
    void rejectsLookalikeDomains() {
        assertThrows(IllegalArgumentException.class,
                () -> AccountRules.normalizeEmail("marco@studenti.unipd.it.evil.com"));
        assertThrows(IllegalArgumentException.class,
                () -> AccountRules.normalizeEmail("marco@evilstudenti.unipd.it"));
        assertThrows(IllegalArgumentException.class,
                () -> AccountRules.normalizeEmail("a@b@studenti.unipd.it"));
    }

    @Test
    void rejectsMissingEmail() {
        assertThrows(IllegalArgumentException.class, () -> AccountRules.normalizeEmail(null));
    }

    @Test
    void checksPasswordLength() {
        assertThrows(IllegalArgumentException.class, () -> AccountRules.checkPassword("123456789"));
        assertDoesNotThrow(() -> AccountRules.checkPassword("1234567890"));
        assertThrows(IllegalArgumentException.class, () -> AccountRules.checkPassword("x".repeat(201)));
    }
}