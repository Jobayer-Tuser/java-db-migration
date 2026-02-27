package me.jobayeralmahmud.javamigrations.library;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Consumer;

public class Schema {

    private final Connection connection;

    public Schema(Connection connection) {
        this.connection = connection;
    }

    public void create(String tableName, Consumer<Blueprint> callback) throws SQLException {
        Blueprint table = new Blueprint(tableName);
        callback.accept(table);
        execute(table.getSql(tableName));
    }

    public void dropIfExists(String tableName) throws SQLException {
        execute("DROP TABLE IF EXISTS " + tableName);
    }

    public void table(String tableName, Consumer<Blueprint> callback) throws SQLException {
        Blueprint table = new Blueprint(tableName);
        table.setAlterMode();
        callback.accept(table);

        connection.setAutoCommit(false);
        try (Statement statement = connection.createStatement()) {
            for (String sql : table.getAlterationSql(tableName)) {
                statement.execute(sql);
                connection.commit();
            }
        } catch (SQLException e) {
            connection.rollback();
            throw e; // re-throw so the migration failure is visible
        }
    }

    private void execute(String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}