package me.jobayeralmahmud.javamigrations.autoconfig;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * Spring Boot Auto-Configuration for the core Java custom migration DSL
 * library.
 *
 * <p>
 * This configuration is activated automatically when:
 * <ul>
 * <li>The library jar is on the classpath</li>
 * </ul>
 */
@Configuration
//@ConditionalOnProperties(value = {})
@ComponentScan(basePackages = "me.jobayeralmahmud.javamigrations")
public class JavaMigrationAutoConfigurer {

    @Bean
    public MigrationInitializer migrationInitializer(
            DataSource dataSource,
            ObjectProvider<List<BaseMigration>> migrationsProvider) {

        List<BaseMigration> migrations = migrationsProvider.getIfAvailable(ArrayList::new);
        return new MigrationInitializer(dataSource, migrations);
    }
}