package me.jobayeralmahmud.javamigrations;

import org.junit.jupiter.api.Test;

import me.jobayeralmahmud.javamigrations.library.Blueprint;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link Blueprint} SQL generation.
 * No database connection required.
 */
class BlueprintSqlGenerationTest {

    @Test
    void basicTableSql_containsExpectedParts() {
        Blueprint table = new Blueprint("roles");

        table.id();
        table.string("name");
        table.timestamps();

        String sql = table.getSql("roles");

        assertThat(sql).startsWith("CREATE TABLE roles (");
        assertThat(sql).contains("id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY");
        assertThat(sql).contains("name VARCHAR(255) NOT NULL");
        assertThat(sql).contains("created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
    }

    @Test
    void foreignIdColumn_generatesConstraint() {
        Blueprint table = new Blueprint("users");

        table.id();
        table.foreignId("role_id").constrained().onUpdateCascade().onDeleteRestrict();
        table.string("email").unique();

        String sql = table.getSql("users");

        assertThat(sql).contains("role_id BIGINT UNSIGNED NOT NULL");
        assertThat(sql).contains("CONSTRAINT FK_users_role_id FOREIGN KEY (role_id) REFERENCES roles (id)");
        assertThat(sql).contains("ON UPDATE CASCADE");
        assertThat(sql).contains("ON DELETE RESTRICT");
        assertThat(sql).contains("email VARCHAR(255) NOT NULL UNIQUE");
    }

    @Test
    void nullableColumn_useDefaultNull() {
        Blueprint table = new Blueprint("items");

        table.id();
        table.datetime("deleted_at").nullable();

        String sql = table.getSql("items");

        assertThat(sql).contains("deleted_at DATETIME(6) DEFAULT NULL");
    }

    @Test
    void enumColumn_generatesCorrectSql() {
        Blueprint table = new Blueprint("orders");

        table.id();
        table.enumeration("status", "pending", "paid", "cancelled");

        String sql = table.getSql("orders");

        assertThat(sql).contains("status ENUM('pending', 'paid', 'cancelled') NOT NULL");
    }

    @Test
    void selfReferentialForeignKey_worksWithExplicitTable() {
        Blueprint table = new Blueprint("categories");

        table.id();
        table.foreignId("parent_category_id").constrained("categories").onUpdateCascade().onDeleteRestrict();
        table.string("name");

        String sql = table.getSql("categories");

        assertThat(sql).contains("REFERENCES categories (id)");
    }
}
