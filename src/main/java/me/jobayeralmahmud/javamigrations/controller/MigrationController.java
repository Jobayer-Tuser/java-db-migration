package me.jobayeralmahmud.javamigrations.controller;

import me.jobayeralmahmud.javamigrations.migrations.BaseMigration;
import me.jobayeralmahmud.javamigrations.migrations.MigrationRunner;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

@RestController
public class MigrationController {

    private final DataSource dataSource;
    private final List<BaseMigration> migrations;

    public MigrationController(DataSource dataSource, List<BaseMigration> migrations) {
        this.dataSource = dataSource;
        this.migrations = migrations;
    }

    @PostMapping("/api/migrations/rollback")
    public String rollbackMigrations(@RequestParam(defaultValue = "1") int steps) {
        try (Connection connection = dataSource.getConnection()) {
            MigrationRunner runner = new MigrationRunner(connection);
            runner.rollback(migrations, steps);
            return "Successfully rolled back " + steps + " migration(s).";
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to rollback: " + e.getMessage();
        }
    }
}
