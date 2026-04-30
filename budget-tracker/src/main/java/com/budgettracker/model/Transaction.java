package com.budgettracker.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private TransactionType type;
    private String category;
    private String description;
    private double amount;
    private String timestamp;

    public Transaction(TransactionType type, String category, String description, double amount) {
        this.type = type;
        this.category = category;
        this.description = description;
        this.amount = amount;
        this.timestamp = LocalDateTime.now().format(DISPLAY_FORMAT);
    }

    public TransactionType getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        String typeStr = type == TransactionType.INCOME ? "INCOME " : "EXPENSE";
        String categoryStr = (category == null || category.isEmpty()) ? "N/A" : category;
        return String.format("[%s] %s | %-12s | %-25s | $%.2f",
                timestamp, typeStr, categoryStr, description, amount);
    }
}
