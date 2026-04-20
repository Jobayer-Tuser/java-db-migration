package me.jobayeralmahmud.javamigrations.migrations;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

/**
 * Automatically executed by Spring Boot on application startup.
 * It grabs all {@link BaseMigration} beans and runs them against the
 * DataSource.
 */
@Component
public class MigrationInitializer implements InitializingBean {

    private final DataSource dataSource;
    private final List<BaseMigration> migrations;

    public MigrationInitializer(DataSource dataSource, List<BaseMigration> migrations) {
        this.dataSource = dataSource;
        this.migrations = migrations;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (migrations == null || migrations.isEmpty()) {
            System.out.println("No database migrations found to execute.");
            return;
        }

        try (Connection connection = dataSource.getConnection()) {
            MigrationRunner runner = new MigrationRunner(connection);
            runner.run(migrations);
        }
    }
}