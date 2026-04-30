package com.budgettracker.storage;

import com.budgettracker.model.Transaction;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class StorageService {

    private static final Type TRANSACTION_LIST_TYPE =
            new TypeToken<List<Transaction>>() {}.getType();

    private final Path filePath;
    private final Gson gson;

    public StorageService(String filePath) {
        this.filePath = Path.of(filePath);
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public List<Transaction> load() {
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }
        try (Reader reader = Files.newBufferedReader(filePath)) {
            List<Transaction> transactions = gson.fromJson(reader, TRANSACTION_LIST_TYPE);
            return transactions != null ? new ArrayList<>(transactions) : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Error reading data file: " + e.getMessage());
            return new ArrayList<>();
        } catch (com.google.gson.JsonSyntaxException e) {
            System.err.println("Error: Data file is corrupted. Starting with empty data.");
            return new ArrayList<>();
        } catch (JsonIOException e) {
            System.err.println("Error reading data file: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public boolean save(List<Transaction> transactions) {
        try {
            Path parent = filePath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            try (Writer writer = Files.newBufferedWriter(filePath)) {
                gson.toJson(transactions, writer);
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
            return false;
        } catch (JsonIOException e) {
            System.err.println("Error saving data: " + e.getMessage());
            return false;
        }
    }
}
