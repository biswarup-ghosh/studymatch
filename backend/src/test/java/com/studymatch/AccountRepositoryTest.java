package com.studymatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AccountRepositoryTest {
    @TempDir
    Path folder;

    private AccountRepository newRepository() throws SQLException {
        Database database = new Database("jdbc:sqlite:" + folder.resolve("test.db"));
        database.createTables();
        return new AccountRepository(database);
    }

    @Test
    void newAccountStartsUnverified() throws SQLException {
        AccountRepository repository = newRepository();

        assertTrue(repository.create("marco@studenti.unipd.it", "hash", "token1"));
        AccountRepository.Account account = repository.findByEmail("marco@studenti.unipd.it");

        assertEquals("marco@studenti.unipd.it", account.email());
        assertEquals("hash", account.passwordHash());
        assertFalse(account.verified());
        assertNull(repository.findByEmail("someone@studenti.unipd.it"));
    }

    @Test
    void duplicateEmailIsRejected() throws SQLException {
        AccountRepository repository = newRepository();

        assertTrue(repository.create("marco@studenti.unipd.it", "hash", "token1"));
        assertFalse(repository.create("marco@studenti.unipd.it", "other", "token2"));
    }

    @Test
    void verificationTokenWorksOnce() throws SQLException {
        AccountRepository repository = newRepository();
        repository.create("marco@studenti.unipd.it", "hash", "token1");

        assertFalse(repository.verify("wrong-token"));
        assertTrue(repository.verify("token1"));
        assertTrue(repository.findByEmail("marco@studenti.unipd.it").verified());
        assertFalse(repository.verify("token1"));
    }
}