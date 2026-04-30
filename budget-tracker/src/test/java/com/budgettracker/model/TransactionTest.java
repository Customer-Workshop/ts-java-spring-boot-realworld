package com.budgettracker.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    @Test
    void incomeTransactionCreation() {
        Transaction t = new Transaction(TransactionType.INCOME, "", "Salary", 5000.0);
        assertEquals(TransactionType.INCOME, t.getType());
        assertEquals("", t.getCategory());
        assertEquals("Salary", t.getDescription());
        assertEquals(5000.0, t.getAmount());
        assertNotNull(t.getTimestamp());
    }

    @Test
    void expenseTransactionCreation() {
        Transaction t = new Transaction(TransactionType.EXPENSE, "Food", "Groceries", 150.0);
        assertEquals(TransactionType.EXPENSE, t.getType());
        assertEquals("Food", t.getCategory());
        assertEquals("Groceries", t.getDescription());
        assertEquals(150.0, t.getAmount());
    }

    @Test
    void setTimestamp() {
        Transaction t = new Transaction(TransactionType.INCOME, "", "Test", 100.0);
        t.setTimestamp("2024-01-15 10:30:00");
        assertEquals("2024-01-15 10:30:00", t.getTimestamp());
    }

    @Test
    void toStringForIncome() {
        Transaction t = new Transaction(TransactionType.INCOME, "", "Salary", 5000.0);
        t.setTimestamp("2024-01-15 10:30:00");
        String str = t.toString();
        assertTrue(str.contains("INCOME"));
        assertTrue(str.contains("N/A"));
        assertTrue(str.contains("Salary"));
        assertTrue(str.contains("5000.00"));
    }

    @Test
    void toStringForExpense() {
        Transaction t = new Transaction(TransactionType.EXPENSE, "Food", "Groceries", 150.0);
        t.setTimestamp("2024-01-15 10:30:00");
        String str = t.toString();
        assertTrue(str.contains("EXPENSE"));
        assertTrue(str.contains("Food"));
        assertTrue(str.contains("Groceries"));
        assertTrue(str.contains("150.00"));
    }
}
