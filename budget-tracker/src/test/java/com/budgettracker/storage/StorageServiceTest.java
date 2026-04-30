package com.budgettracker.storage;

import com.budgettracker.model.Transaction;
import com.budgettracker.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StorageServiceTest {

    @TempDir
    Path tempDir;

    private StorageService storageService;
    private String filePath;

    @BeforeEach
    void setUp() {
        filePath = tempDir.resolve("test_budget.json").toString();
        storageService = new StorageService(filePath);
    }

    @Test
    void loadReturnsEmptyListWhenFileDoesNotExist() {
        List<Transaction> result = storageService.load();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void saveAndLoadRoundTrip() {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction(TransactionType.INCOME, "", "Salary", 5000.0));
        transactions.add(new Transaction(TransactionType.EXPENSE, "Food", "Groceries", 150.0));

        assertTrue(storageService.save(transactions));

        List<Transaction> loaded = storageService.load();
        assertEquals(2, loaded.size());
        assertEquals("Salary", loaded.get(0).getDescription());
        assertEquals(5000.0, loaded.get(0).getAmount());
        assertEquals(TransactionType.INCOME, loaded.get(0).getType());
        assertEquals("Groceries", loaded.get(1).getDescription());
        assertEquals(150.0, loaded.get(1).getAmount());
        assertEquals("Food", loaded.get(1).getCategory());
    }

    @Test
    void saveCreatesParentDirectories() {
        String nestedPath = tempDir.resolve("sub/dir/data.json").toString();
        StorageService nestedService = new StorageService(nestedPath);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction(TransactionType.INCOME, "", "Test", 100.0));

        assertTrue(nestedService.save(transactions));
        assertEquals(1, nestedService.load().size());
    }

    @Test
    void loadHandlesCorruptedFile() throws IOException {
        Files.writeString(Path.of(filePath), "this is not json{{{");
        List<Transaction> result = storageService.load();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void loadHandlesEmptyFile() throws IOException {
        Files.writeString(Path.of(filePath), "");
        List<Transaction> result = storageService.load();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void loadHandlesNullJsonContent() throws IOException {
        Files.writeString(Path.of(filePath), "null");
        List<Transaction> result = storageService.load();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void saveEmptyList() {
        assertTrue(storageService.save(new ArrayList<>()));
        List<Transaction> loaded = storageService.load();
        assertNotNull(loaded);
        assertTrue(loaded.isEmpty());
    }

    @Test
    void saveToReadOnlyLocationFails() {
        StorageService badService = new StorageService("/proc/invalid/path/data.json");
        assertFalse(badService.save(List.of(new Transaction(TransactionType.INCOME, "", "Test", 100.0))));
    }
}
