package me.jobayeralmahmud.javamigrations.migrations;

import java.sql.SQLException;

public abstract class BaseMigration {

    /**
     * Automatically extracts the version from the class name
     * example(s1_create_users_table).
     */
    public String migrationTablesName() {
        return convertClassNameToSnakeCase(this.getClass().getSimpleName());
    }

    private String convertClassNameToSnakeCase(String className) {
        return className.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    /**
     * Forward migration logic (create tables, insert data, etc.).
     */
    public abstract void up(Schema schema) throws SQLException;

    /**
     * Rollback migration logic (drop tables, remove data, etc.).
     */
    public void down(Schema schema) throws SQLException {
        // Default implementation: do nothing. Override if needed.{
    }

    /**
     * Helper to easily output logs.
     */
    protected void log(String message) {
        System.out.println("✓ " + message);
    }
}