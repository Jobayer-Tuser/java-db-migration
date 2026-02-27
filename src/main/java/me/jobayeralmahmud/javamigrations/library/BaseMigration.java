package me.jobayeralmahmud.javamigrations.library;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class BaseMigration {

    /**
     * Automatically extracts the version from the class name.
     * Expects a format like V20231024_01__Create_table.
     * Extracts and returns the version part (e.g., "20231024_01").
     */
    public String getVersion() {
        return currentDateTime().concat(convertClassNameToSnakeCase(this.getClass().getSimpleName()));
    }

    private String convertClassNameToSnakeCase(String className) {
        return className.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    private String currentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern("yyyy_MM_dd");
        return now.format(customFormatter);
    }

    /**
     * Forward migration logic (create tables, insert data, etc.).
     */
    public abstract void up(Schema schema) throws SQLException;

    /**
     * Rollback migration logic (drop tables, remove data, etc.).
     */
    public abstract void down(Schema schema) throws SQLException;

    /**
     * Helper to easily output logs.
     */
    protected void log(String message) {
        System.out.println("✓ " + message);
    }
}
