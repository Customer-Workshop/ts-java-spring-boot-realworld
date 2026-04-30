package com.budgettracker.service;

import com.budgettracker.model.Transaction;
import com.budgettracker.model.TransactionType;
import com.budgettracker.storage.StorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BudgetServiceTest {

    @TempDir
    Path tempDir;

    private BudgetService budgetService;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        String filePath = tempDir.resolve("test_budget.json").toString();
        StorageService storageService = new StorageService(filePath);
        budgetService = new BudgetService(storageService);
        originalOut = System.out;
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void addIncomeSuccessfully() {
        Transaction t = budgetService.addIncome("Salary", 5000.0);
        assertEquals(TransactionType.INCOME, t.getType());
        assertEquals("Salary", t.getDescription());
        assertEquals(5000.0, t.getAmount());
        assertEquals("", t.getCategory());
    }

    @Test
    void addIncomeTrimsDescription() {
        Transaction t = budgetService.addIncome("  Freelance Work  ", 1000.0);
        assertEquals("Freelance Work", t.getDescription());
    }

    @Test
    void addIncomeRejectsEmptyDescription() {
        assertThrows(IllegalArgumentException.class, () -> budgetService.addIncome("", 100.0));
        assertThrows(IllegalArgumentException.class, () -> budgetService.addIncome("   ", 100.0));
        assertThrows(IllegalArgumentException.class, () -> budgetService.addIncome(null, 100.0));
    }

    @Test
    void addIncomeRejectsNonPositiveAmount() {
        assertThrows(IllegalArgumentException.class, () -> budgetService.addIncome("Salary", 0));
        assertThrows(IllegalArgumentException.class, () -> budgetService.addIncome("Salary", -100));
    }

    @Test
    void addExpenseSuccessfully() {
        Transaction t = budgetService.addExpense("Food", "Groceries", 150.0);
        assertEquals(TransactionType.EXPENSE, t.getType());
        assertEquals("Food", t.getCategory());
        assertEquals("Groceries", t.getDescription());
        assertEquals(150.0, t.getAmount());
    }

    @Test
    void addExpenseTrimsInputs() {
        Transaction t = budgetService.addExpense("  Transport  ", "  Bus fare  ", 50.0);
        assertEquals("Transport", t.getCategory());
        assertEquals("Bus fare", t.getDescription());
    }

    @Test
    void addExpenseRejectsEmptyCategory() {
        assertThrows(IllegalArgumentException.class,
                () -> budgetService.addExpense("", "Groceries", 100.0));
        assertThrows(IllegalArgumentException.class,
                () -> budgetService.addExpense(null, "Groceries", 100.0));
    }

    @Test
    void addExpenseRejectsEmptyDescription() {
        assertThrows(IllegalArgumentException.class,
                () -> budgetService.addExpense("Food", "", 100.0));
        assertThrows(IllegalArgumentException.class,
                () -> budgetService.addExpense("Food", null, 100.0));
    }

    @Test
    void addExpenseRejectsNonPositiveAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> budgetService.addExpense("Food", "Groceries", 0));
        assertThrows(IllegalArgumentException.class,
                () -> budgetService.addExpense("Food", "Groceries", -50));
    }

    @Test
    void getTotalIncome() {
        budgetService.addIncome("Salary", 5000.0);
        budgetService.addIncome("Bonus", 1000.0);
        budgetService.addExpense("Food", "Groceries", 200.0);
        assertEquals(6000.0, budgetService.getTotalIncome(), 0.01);
    }

    @Test
    void getTotalExpenses() {
        budgetService.addIncome("Salary", 5000.0);
        budgetService.addExpense("Food", "Groceries", 200.0);
        budgetService.addExpense("Transport", "Bus", 50.0);
        assertEquals(250.0, budgetService.getTotalExpenses(), 0.01);
    }

    @Test
    void getNetBalance() {
        budgetService.addIncome("Salary", 5000.0);
        budgetService.addExpense("Rent", "Monthly rent", 1500.0);
        budgetService.addExpense("Food", "Groceries", 300.0);
        assertEquals(3200.0, budgetService.getNetBalance(), 0.01);
    }

    @Test
    void getNetBalanceIsZeroWhenNoTransactions() {
        assertEquals(0.0, budgetService.getNetBalance(), 0.01);
    }

    @Test
    void getTransactionsReturnsUnmodifiableList() {
        budgetService.addIncome("Salary", 5000.0);
        List<Transaction> transactions = budgetService.getTransactions();
        assertThrows(UnsupportedOperationException.class,
                () -> transactions.add(new Transaction(TransactionType.INCOME, "", "Hack", 1.0)));
    }

    @Test
    void getTransactionsInChronologicalOrder() {
        budgetService.addIncome("First", 100.0);
        budgetService.addExpense("Cat", "Second", 200.0);
        budgetService.addIncome("Third", 300.0);

        List<Transaction> transactions = budgetService.getTransactions();
        assertEquals(3, transactions.size());
        assertEquals("First", transactions.get(0).getDescription());
        assertEquals("Second", transactions.get(1).getDescription());
        assertEquals("Third", transactions.get(2).getDescription());
    }

    @Test
    void printSummaryOutputsCorrectFormat() {
        budgetService.addIncome("Salary", 5000.0);
        budgetService.addExpense("Food", "Groceries", 200.0);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        budgetService.printSummary();

        String output = out.toString();
        assertTrue(output.contains("BUDGET SUMMARY"));
        assertTrue(output.contains("5000.00"));
        assertTrue(output.contains("200.00"));
        assertTrue(output.contains("4800.00"));
    }

    @Test
    void printTransactionsWhenEmpty() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        budgetService.printTransactions();

        assertTrue(out.toString().contains("No transactions recorded yet."));
    }

    @Test
    void printTransactionsShowsAllEntries() {
        budgetService.addIncome("Salary", 5000.0);
        budgetService.addExpense("Food", "Groceries", 200.0);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        budgetService.printTransactions();

        String output = out.toString();
        assertTrue(output.contains("ALL TRANSACTIONS"));
        assertTrue(output.contains("Salary"));
        assertTrue(output.contains("Groceries"));
        assertTrue(output.contains("2 transaction(s)"));
    }

    @Test
    void dataPersistsAcrossServiceInstances() {
        String filePath = tempDir.resolve("persist_test.json").toString();
        StorageService storage = new StorageService(filePath);
        BudgetService service1 = new BudgetService(storage);

        service1.addIncome("Salary", 3000.0);
        service1.addExpense("Food", "Lunch", 15.0);

        BudgetService service2 = new BudgetService(new StorageService(filePath));
        assertEquals(2, service2.getTransactions().size());
        assertEquals(3000.0, service2.getTotalIncome(), 0.01);
        assertEquals(15.0, service2.getTotalExpenses(), 0.01);
    }
}
