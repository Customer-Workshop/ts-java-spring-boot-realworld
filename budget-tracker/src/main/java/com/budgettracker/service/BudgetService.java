package com.budgettracker.service;

import com.budgettracker.model.Transaction;
import com.budgettracker.model.TransactionType;
import com.budgettracker.storage.StorageService;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class BudgetService {

    private final List<Transaction> transactions;
    private final StorageService storageService;

    public BudgetService(StorageService storageService) {
        this.storageService = storageService;
        this.transactions = storageService.load();
    }

    public Transaction addIncome(String description, double amount) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty.");
        }
        if (amount <= 0 || !Double.isFinite(amount)) {
            throw new IllegalArgumentException("Amount must be a positive number.");
        }
        Transaction transaction = new Transaction(TransactionType.INCOME, "", description.trim(), amount);
        transactions.add(transaction);
        if (!storageService.save(transactions)) {
            transactions.remove(transactions.size() - 1);
            throw new RuntimeException("Failed to save transaction to disk.");
        }
        return transaction;
    }

    public Transaction addExpense(String category, String description, double amount) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be empty.");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty.");
        }
        if (amount <= 0 || !Double.isFinite(amount)) {
            throw new IllegalArgumentException("Amount must be a positive number.");
        }
        Transaction transaction = new Transaction(
                TransactionType.EXPENSE, category.trim(), description.trim(), amount);
        transactions.add(transaction);
        if (!storageService.save(transactions)) {
            transactions.remove(transactions.size() - 1);
            throw new RuntimeException("Failed to save transaction to disk.");
        }
        return transaction;
    }

    public double getTotalIncome() {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getTotalExpenses() {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getNetBalance() {
        return getTotalIncome() - getTotalExpenses();
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public Set<String> getCategories() {
        return transactions.stream()
                .map(Transaction::getCategory)
                .filter(c -> c != null && !c.isEmpty())
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public List<Transaction> filterByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be empty.");
        }
        String target = category.trim().toLowerCase();
        return transactions.stream()
                .filter(t -> t.getCategory() != null
                        && t.getCategory().toLowerCase().equals(target))
                .collect(Collectors.toList());
    }

    public void printFilteredTransactions(String category) {
        List<Transaction> filtered = filterByCategory(category);
        System.out.println();
        if (filtered.isEmpty()) {
            System.out.printf("No transactions found for category: %s%n", category.trim());
            System.out.println();
            return;
        }
        System.out.printf("============ TRANSACTIONS: %s ============%n", category.trim().toUpperCase());
        System.out.printf("%-22s %-8s %-14s %-25s %s%n",
                "Date/Time", "Type", "Category", "Description", "Amount");
        System.out.println("--------------------------------------------------------");
        for (Transaction t : filtered) {
            System.out.println(t);
        }
        System.out.println("========================================================");
        System.out.printf("Total: %d transaction(s)%n", filtered.size());
        System.out.println();
    }

    public void printSummary() {
        double income = getTotalIncome();
        double expenses = getTotalExpenses();
        double balance = getNetBalance();

        System.out.println();
        System.out.println("==================== BUDGET SUMMARY ====================");
        System.out.printf("  Total Income:   $%10.2f%n", income);
        System.out.printf("  Total Expenses: $%10.2f%n", expenses);
        System.out.println("--------------------------------------------------------");
        System.out.printf("  Net Balance:    $%10.2f%n", balance);
        System.out.println("========================================================");
        System.out.println();
    }

    public void printTransactions() {
        System.out.println();
        if (transactions.isEmpty()) {
            System.out.println("No transactions recorded yet.");
            System.out.println();
            return;
        }
        System.out.println("=================== ALL TRANSACTIONS ===================");
        System.out.printf("%-22s %-8s %-14s %-25s %s%n",
                "Date/Time", "Type", "Category", "Description", "Amount");
        System.out.println("--------------------------------------------------------");
        for (Transaction t : transactions) {
            System.out.println(t);
        }
        System.out.println("========================================================");
        System.out.printf("Total: %d transaction(s)%n", transactions.size());
        System.out.println();
    }
}
