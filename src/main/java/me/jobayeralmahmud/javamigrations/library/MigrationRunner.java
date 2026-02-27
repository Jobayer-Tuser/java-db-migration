package me.jobayeralmahmud.javamigrations.library;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A purely core Java database migration runner without Flyway dependencies.
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
        migrations.sort((m1, m2) -> m1.getVersion().compareTo(m2.getVersion()));

        for (BaseMigration migration : migrations) {
            if (!executed.contains(migration.getVersion())) {
                System.out.println("Running migration: " + migration.getVersion());
                runMigration(migration);
                recordMigration(migration.getVersion());
            } else {
                System.out.println("Skipping migration: " + migration.getVersion() + " (already executed)");
            }
        }
    }

    private void runMigration(BaseMigration migration) throws SQLException {
        boolean originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        Schema schema = new Schema(connection);

        try {
            // 1. Run the forward migration
            migration.up(schema);

            // 2. Commit the transaction
            connection.commit();
            System.out.println("✓ Migration " + migration.getVersion() + " applied successfully.");

        } catch (Exception e) {
            System.err.println("❌ Migration " + migration.getVersion() + " failed! Reverting transaction.");

            // 3. Rollback the active transaction
            connection.rollback();

            // 4. Compensation action logic: since DDL (CREATE/ALTER TABLE) auto-commits
            // implicitly on DBs like MySQL (meaning connection.rollback() might do nothing
            // for DDL),
            // we execute the down() method to explicitly drop what was created/modified.
            try {
                System.out.println("Attempting to run down() method for compensation...");
                migration.down(schema);
                connection.commit();
                System.out.println("✓ down() compensation logic applied successfully.");
            } catch (Exception compensationEx) {
                connection.rollback();
                System.err.println("❌ down() explicitly failed too: " + compensationEx.getMessage());
            }

            // Re-throw to stop further migrations
            throw new SQLException("Migration failed: " + migration.getVersion(), e);

        } finally {
            // Restore autocommit mode
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    private void ensureMigrationsTableExists() throws SQLException {
        // We ensure a simple schema_migrations table exists for tracking
        String sql = """
                CREATE TABLE IF NOT EXISTS schema_migrations (
                    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                    version VARCHAR(255) NOT NULL UNIQUE,
                    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    private List<String> getExecutedMigrations() throws SQLException {
        List<String> executed = new ArrayList<>();
        String sql = "SELECT version FROM schema_migrations ORDER BY id ASC";
        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                executed.add(rs.getString("version"));
            }
        }
        return executed;
    }

    private void recordMigration(String version) throws SQLException {
        boolean autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        String sql = "INSERT INTO schema_migrations (version) VALUES (?)";
        try (PreparedStatement check = connection.prepareStatement(sql)) {
            check.setString(1, version);
            check.executeUpdate();
            connection.commit();
        } finally {
            connection.setAutoCommit(autoCommit);
        }
    }
}
