package me.jobayeralmahmud.javamigrations.migrations;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A purely core Java database migration runner without any dependencies.
 * Automatically tracks executed migrations and rolls back failed ones.
 */
public class MigrationRunner {

    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS schema_migrations (
                id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                migration_tables VARCHAR(255) NOT NULL UNIQUE,
                executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;

    private static final String SELECT_MIGRATIONS_SQL =
            "SELECT migration_tables FROM schema_migrations ORDER BY id ASC";

    private static final String INSERT_MIGRATION_SQL =
            "INSERT INTO schema_migrations (migration_tables) VALUES (?)";

    private static final String DELETE_MIGRATION_SQL =
            "DELETE FROM schema_migrations WHERE migration_tables = ?";

    private final Connection connection;

    public MigrationRunner(Connection connection) {
        this.connection = connection;
    }

    /**
     * Runs pending migrations in sequential order, skipping already-executed ones.
     */
    public void run(List<BaseMigration> migrations) throws SQLException {
        ensureMigrationsTableExists();
        List<String> executed = getExecutedMigrations();

        migrations.stream()
                .sorted(Comparator.comparing(BaseMigration::migrationTablesName))
                .filter(m -> !executed.contains(m.migrationTablesName()))
                .forEach(m -> {
                    try {
                        info("Running migration: " + m.migrationTablesName());
                        runMigration(m);
                        recordMigration(m.migrationTablesName());
                    } catch (SQLException e) {
                        throw new RuntimeException(e.getMessage());
                    }
                });
    }

    /**
     * Reverts the most recently executed migrations.
     *
     * @param migrations The full list of available migration beans.
     * @param steps      How many migrations to roll back. Use {@link Integer#MAX_VALUE} for all.
     */
    public void rollback(List<BaseMigration> migrations, int steps) throws SQLException {
        ensureMigrationsTableExists();

        List<String> executed = getExecutedMigrations();
        if (executed.isEmpty()) {
            info("No migrations to rollback.");
            return;
        }

        List<String> toRollback = new ArrayList<>(executed.subList(Math.max(0, executed.size() - steps), executed.size()));
        Collections.reverse(toRollback);

        for (String name : toRollback) {
            BaseMigration migration = migrations.stream()
                    .filter(m -> m.migrationTablesName().equals(name))
                    .findFirst()
                    .orElse(null);

            if (migration == null) {
                error("Cannot rollback " + name + ": migration class not found.");
                continue;
            }

            info("Rolling back migration: " + name);
            withTransaction(schema -> {
                migration.down(schema);
                removeMigrationRecord(name);
                info("Rollback of " + name + " successful.");
            }, "Rollback failed: " + name);
        }
    }

    // --- Core helpers ---

    private void runMigration(BaseMigration migration) throws SQLException {
        withTransaction(schema -> migration.up(schema), "Migration failed: " + migration.migrationTablesName());
    }

    /**
     * Runs {@code action} inside a transaction. On failure, attempts rollback,
     * then re-throws as {@link SQLException}.
     */
    private void withTransaction(ThrowingConsumer<Schema> action, String errorMessage) throws SQLException {
        boolean originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            action.accept(new Schema(connection));
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw new SQLException(errorMessage, e);
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    private void executePrepared(String sql, String value) throws SQLException {
        boolean autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, value);
            stmt.executeUpdate();
            connection.commit();
        } finally {
            connection.setAutoCommit(autoCommit);
        }
    }

    // --- DB operations ---

    private void ensureMigrationsTableExists() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(CREATE_TABLE_SQL);
        }
    }

    private List<String> getExecutedMigrations() throws SQLException {
        List<String> executed = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_MIGRATIONS_SQL)) {
            while (rs.next()) executed.add(rs.getString("migration_tables"));
        }
        return executed;
    }

    private void recordMigration(String name) throws SQLException {
        executePrepared(INSERT_MIGRATION_SQL, name);
    }

    private void removeMigrationRecord(String name) throws SQLException {
        executePrepared(DELETE_MIGRATION_SQL, name);
    }

    // --- Logging ---

    private static void info(String message)  { System.out.println(message); }
    private static void error(String message) { System.err.println(message); }

    // --- Functional interface ---

    @FunctionalInterface
    private interface ThrowingConsumer<T> {
        void accept(T t) throws Exception;
    }
}