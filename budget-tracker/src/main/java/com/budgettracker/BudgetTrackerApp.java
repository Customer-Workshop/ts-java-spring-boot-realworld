package com.budgettracker;

import com.budgettracker.service.BudgetService;
import com.budgettracker.storage.StorageService;

import java.util.Scanner;
import java.util.Set;

public class BudgetTrackerApp {

    private static final String DATA_FILE = "budget_data.json";
    private static final String MENU = """

            ============ BUDGET TRACKER ============
              1. Add Income
              2. Add Expense
              3. View Summary
              4. List All Transactions
              5. Filter by Category
              6. Exit
            ========================================
            Choose an option (1-6):\s""";

    private final BudgetService budgetService;
    private final Scanner scanner;

    public BudgetTrackerApp(BudgetService budgetService, Scanner scanner) {
        this.budgetService = budgetService;
        this.scanner = scanner;
    }

    public static void main(String[] args) {
        StorageService storageService = new StorageService(DATA_FILE);
        BudgetService budgetService = new BudgetService(storageService);
        Scanner scanner = new Scanner(System.in);

        BudgetTrackerApp app = new BudgetTrackerApp(budgetService, scanner);
        app.run();
    }

    public void run() {
        System.out.println("Welcome to Budget Tracker!");
        System.out.println("Your data is saved to: " + DATA_FILE);

        boolean running = true;
        while (running) {
            System.out.print(MENU);
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> handleAddIncome();
                case "2" -> handleAddExpense();
                case "3" -> budgetService.printSummary();
                case "4" -> budgetService.printTransactions();
                case "5" -> handleFilterByCategory();
                case "6" -> {
                    System.out.println("Goodbye!");
                    running = false;
                }
                default -> System.out.println("Error: Invalid option. Please enter a number between 1 and 6.");
            }
        }
    }

    void handleAddIncome() {
        System.out.print("Enter description: ");
        String description = scanner.nextLine();

        double amount = readAmount();
        if (amount < 0) {
            return;
        }

        try {
            budgetService.addIncome(description, amount);
            System.out.printf("Income added: %s - $%.2f%n", description.trim(), amount);
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    void handleAddExpense() {
        System.out.print("Enter category (e.g., Food, Transport, Rent): ");
        String category = scanner.nextLine();

        System.out.print("Enter description: ");
        String description = scanner.nextLine();

        double amount = readAmount();
        if (amount < 0) {
            return;
        }

        try {
            budgetService.addExpense(category, description, amount);
            System.out.printf("Expense added: [%s] %s - $%.2f%n",
                    category.trim(), description.trim(), amount);
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    void handleFilterByCategory() {
        Set<String> categories = budgetService.getCategories();
        if (categories.isEmpty()) {
            System.out.println("No categories found. Add some expenses first.");
            return;
        }
        System.out.println("Available categories: " + String.join(", ", categories));
        System.out.print("Enter category to filter by: ");
        String category = scanner.nextLine();
        try {
            budgetService.printFilteredTransactions(category);
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    double readAmount() {
        System.out.print("Enter amount: ");
        String input = scanner.nextLine().trim();
        try {
            double amount = Double.parseDouble(input);
            if (amount <= 0 || !Double.isFinite(amount)) {
                System.out.println("Error: Amount must be a positive number.");
                return -1;
            }
            return amount;
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid amount. Please enter a valid number (e.g., 100.50).");
            return -1;
        }
    }
}
