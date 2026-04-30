package com.budgettracker;

import com.budgettracker.service.BudgetService;
import com.budgettracker.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class BudgetTrackerAppTest {

    @TempDir
    Path tempDir;

    private BudgetService budgetService;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        String filePath = tempDir.resolve("test.json").toString();
        StorageService storageService = new StorageService(filePath);
        budgetService = new BudgetService(storageService);
        originalOut = System.out;
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    private BudgetTrackerApp createApp(String input) {
        Scanner scanner = new Scanner(input);
        return new BudgetTrackerApp(budgetService, scanner);
    }

    @Test
    void addIncomeFlow() {
        String input = "1\nSalary\n5000\n5\n";
        BudgetTrackerApp app = createApp(input);
        app.run();

        String output = outputStream.toString();
        assertTrue(output.contains("Income added: Salary - $5000.00"));
        assertEquals(1, budgetService.getTransactions().size());
        assertEquals(5000.0, budgetService.getTotalIncome(), 0.01);

    }

    @Test
    void addExpenseFlow() {
        String input = "2\nFood\nGroceries\n150.50\n5\n";
        BudgetTrackerApp app = createApp(input);
        app.run();

        String output = outputStream.toString();
        assertTrue(output.contains("Expense added: [Food] Groceries - $150.50"));
        assertEquals(1, budgetService.getTransactions().size());
        assertEquals(150.50, budgetService.getTotalExpenses(), 0.01);

    }

    @Test
    void viewSummaryFlow() {
        budgetService.addIncome("Salary", 5000);
        budgetService.addExpense("Food", "Lunch", 25);

        String input = "3\n5\n";
        BudgetTrackerApp app = createApp(input);
        app.run();

        String output = outputStream.toString();
        assertTrue(output.contains("BUDGET SUMMARY"));

    }

    @Test
    void listTransactionsFlow() {
        budgetService.addIncome("Salary", 3000);
        budgetService.addExpense("Rent", "Monthly", 1200);

        String input = "4\n5\n";
        BudgetTrackerApp app = createApp(input);
        app.run();

        String output = outputStream.toString();
        assertTrue(output.contains("ALL TRANSACTIONS"));
        assertTrue(output.contains("Salary"));
        assertTrue(output.contains("Monthly"));

    }

    @Test
    void invalidMenuOption() {
        String input = "9\n5\n";
        BudgetTrackerApp app = createApp(input);
        app.run();

        String output = outputStream.toString();
        assertTrue(output.contains("Invalid option"));

    }

    @Test
    void invalidAmountShowsError() {
        String input = "1\nSalary\nabc\n5\n";
        BudgetTrackerApp app = createApp(input);
        app.run();

        String output = outputStream.toString();
        assertTrue(output.contains("Invalid amount"));
        assertEquals(0, budgetService.getTransactions().size());

    }

    @Test
    void negativeAmountShowsError() {
        String input = "1\nSalary\n-100\n5\n";
        BudgetTrackerApp app = createApp(input);
        app.run();

        String output = outputStream.toString();
        assertTrue(output.contains("must be a positive number"));
        assertEquals(0, budgetService.getTransactions().size());

    }

    @Test
    void zeroAmountShowsError() {
        String input = "2\nFood\nLunch\n0\n5\n";
        BudgetTrackerApp app = createApp(input);
        app.run();

        String output = outputStream.toString();
        assertTrue(output.contains("must be a positive number"));

    }

    @Test
    void emptyDescriptionForIncomeShowsError() {
        String input = "1\n\n100\n5\n";
        BudgetTrackerApp app = createApp(input);
        app.run();

        String output = outputStream.toString();
        assertTrue(output.contains("Description cannot be empty"));

    }

    @Test
    void emptyFieldsForExpenseShowsError() {
        String input = "2\n\nGroceries\n100\n5\n";
        BudgetTrackerApp app = createApp(input);
        app.run();

        String output = outputStream.toString();
        assertTrue(output.contains("Category cannot be empty"));

    }

    @Test
    void exitShowsGoodbye() {
        String input = "5\n";
        BudgetTrackerApp app = createApp(input);
        app.run();

        assertTrue(outputStream.toString().contains("Goodbye!"));

    }

    @Test
    void multipleOperationsInSequence() {
        String input = "1\nSalary\n5000\n2\nFood\nGroceries\n200\n3\n4\n5\n";
        BudgetTrackerApp app = createApp(input);
        app.run();

        String output = outputStream.toString();
        assertTrue(output.contains("Income added"));
        assertTrue(output.contains("Expense added"));
        assertTrue(output.contains("BUDGET SUMMARY"));
        assertTrue(output.contains("ALL TRANSACTIONS"));
        assertEquals(2, budgetService.getTransactions().size());

    }

    @Test
    void handleAddIncomeDirectly() {
        String input = "Test Income\n250.75\n";
        BudgetTrackerApp app = createApp(input);
        app.handleAddIncome();

        String output = outputStream.toString();
        assertTrue(output.contains("Income added: Test Income - $250.75"));

    }

    @Test
    void handleAddExpenseDirectly() {
        String input = "Transport\nBus fare\n3.50\n";
        BudgetTrackerApp app = createApp(input);
        app.handleAddExpense();

        String output = outputStream.toString();
        assertTrue(output.contains("Expense added: [Transport] Bus fare - $3.50"));

    }

    @Test
    void readAmountWithValidInput() {
        String input = "42.99\n";
        BudgetTrackerApp app = createApp(input);
        double amount = app.readAmount();
        assertEquals(42.99, amount, 0.01);

    }

    @Test
    void readAmountWithInvalidInput() {
        String input = "not_a_number\n";
        BudgetTrackerApp app = createApp(input);
        double amount = app.readAmount();
        assertEquals(-1, amount, 0.01);

    }

    @Test
    void readAmountWithNegativeInput() {
        String input = "-50\n";
        BudgetTrackerApp app = createApp(input);
        double amount = app.readAmount();
        assertEquals(-1, amount, 0.01);

    }

    @Test
    void readAmountRejectsNaN() {
        String input = "NaN\n";
        BudgetTrackerApp app = createApp(input);
        double amount = app.readAmount();
        assertEquals(-1, amount, 0.01);
    }

    @Test
    void readAmountRejectsInfinity() {
        String input = "Infinity\n";
        BudgetTrackerApp app = createApp(input);
        double amount = app.readAmount();
        assertEquals(-1, amount, 0.01);
    }
}
