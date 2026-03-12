package me.jobayeralmahmud.javamigrations.migrations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A purely core Java database migration runner without any dependencies.
 * Automatically tracks executed migrations and rolls back failed ones.
 */
public class MigrationRunner {

    private final Connection connection;

    public MigrationRunner(Connection connection) {
        this.connection = connection;
    }

    /**
     * Runs a list of migrations. You can pass them ordered manually or
     * scan your package to provide the instances.
     */
    public void run(List<BaseMigration> migrations) throws SQLException {
        ensureMigrationsTableExists();
        List<String> executed = getExecutedMigrations();

        // Ensure migrations evaluate in sequential order by string representation
        migrations.sort(Comparator.comparing(BaseMigration::migrationTablesName));

        for (BaseMigration migration : migrations) {
            if (!executed.contains(migration.migrationTablesName())) {
                info("Running migration: " + migration.migrationTablesName());
                runMigration(migration);
                recordMigration(migration.migrationTablesName());
            } else {
                info("Skipping migration: " + migration.migrationTablesName() + " (already executed)");
            }
        }
    }

    private void runMigration(BaseMigration migration) throws SQLException {
        boolean originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        Schema schema = new Schema(connection);

        try {
            migration.up(schema);
            connection.commit();
            info("✓ Migration " + migration.migrationTablesName() + " applied successfully.");

        } catch (Exception e) {
            error("❌ Migration " + migration.migrationTablesName() + " failed! Reverting transaction.");
            connection.rollback();

            try {
                info("Attempting to run down() method for compensation...");
                migration.down(schema);
                connection.commit();
                info("✓ down() compensation logic applied successfully.");
            } catch (Exception compensationEx) {
                connection.rollback();
                error("❌ down() explicitly failed too: " + compensationEx.getMessage());
            }

            // Re-throw to stop further migrations
            throw new SQLException("Migration failed: " + migration.migrationTablesName(), e);

        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    /**
     * Reverts previously executed migrations.
     *
     * @param migrations The full list of available migration beans
     * @param steps      How many migrations to roll back (e.g., 1 for the latest).
     *                   Use Integer.MAX_VALUE to roll back everything.
     */
    public void rollback(List<BaseMigration> migrations, int steps) throws SQLException {
        ensureMigrationsTableExists();

        List<String> executed = getExecutedMigrations();
        if (executed.isEmpty()) {
            info("No migrations to rollback.");
            return;
        }

        // We want to rollback the most recently executed migrations first.
        // getExecutedMigrations() returns them ordered by ID ASC, so we reverse it.
        List<String> toRollbackNames = executed.subList(
                Math.max(0, executed.size() - steps),
                executed.size());
        // Reverse them explicitly so we process the absolute latest first
        java.util.Collections.reverse(toRollbackNames);

        for (String executedName : toRollbackNames) {
            // Find the physical bean matching this name
            BaseMigration migration = migrations.stream()
                    .filter(m -> m.migrationTablesName().equals(executedName))
                    .findFirst()
                    .orElse(null);

            if (migration == null) {
                error("❌ Cannot rollback " + executedName + ": Migration class not found!");
                continue;
            }

            info("Rolling back migration: " + executedName);
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            Schema schema = new Schema(connection);

            try {
                migration.down(schema);
                connection.commit();

                // Remove the record from tracking table so it can be re-run in the future
                removeMigrationRecord(executedName);

                info("↩️ Rollback of " + executedName + " successful.");
            } catch (Exception e) {
                error("❌ Rollback of " + executedName + " failed! Reverting transaction.");
                connection.rollback();
                throw new SQLException("Rollback failed: " + executedName, e);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }

    private void ensureMigrationsTableExists() throws SQLException {
        // We ensure a simple schema_migrations table exists for tracking
        String sql = """
                CREATE TABLE IF NOT EXISTS schema_migrations (
                    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                    migration_tables VARCHAR(255) NOT NULL UNIQUE,
                    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    private List<String> getExecutedMigrations() throws SQLException {
        List<String> executed = new ArrayList<>();
        String sql = "SELECT migration_tables FROM schema_migrations ORDER BY id ASC";
        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                executed.add(rs.getString("migration_tables"));
            }
        }
        return executed;
    }

    private void recordMigration(String migrationTables) throws SQLException {
        boolean autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        String sql = "INSERT INTO schema_migrations (migration_tables) VALUES (?)";
        try (PreparedStatement check = connection.prepareStatement(sql)) {
            check.setString(1, migrationTables);
            check.executeUpdate();
            connection.commit();
        } finally {
            connection.setAutoCommit(autoCommit);
        }
    }

    private void removeMigrationRecord(String migrationTables) throws SQLException {
        boolean autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        String sql = "DELETE FROM schema_migrations WHERE migration_tables = ?";
        try (PreparedStatement check = connection.prepareStatement(sql)) {
            check.setString(1, migrationTables);
            check.executeUpdate();
            connection.commit();
        } finally {
            connection.setAutoCommit(autoCommit);
        }
    }

    private static void error(String message) {
        System.err.println(message);
    }

    private static void info(String message) {
        System.out.println(message);
    }
}
