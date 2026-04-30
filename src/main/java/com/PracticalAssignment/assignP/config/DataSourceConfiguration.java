package com.PracticalAssignment.assignP.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Configuration to properly parse Heroku's DATABASE_URL environment variable for PostgreSQL.
 * Heroku provides DATABASE_URL in format: postgres://user:password@host:port/dbname
 * This needs to be converted to JDBC format: jdbc:postgresql://host:port/dbname
 */
@Configuration
@Profile("postgres")
public class DataSourceConfiguration {

    @Bean
    public DataSource dataSource() throws URISyntaxException {
        String databaseUrl = System.getenv("DATABASE_URL");
        
        if (databaseUrl == null || databaseUrl.isEmpty()) {
            throw new IllegalStateException("DATABASE_URL environment variable is not set. " +
                    "Heroku PostgreSQL add-on must be installed and DATABASE_URL must be configured.");
        }

        // Parse DATABASE_URL (format: postgres://user:password@host:port/dbname)
        URI dbUri = new URI(databaseUrl);
        
        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String host = dbUri.getHost();
        int port = dbUri.getPort();
        String database = dbUri.getPath().substring(1); // Remove leading slash
        
        // Build JDBC URL for PostgreSQL
        String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
        
        System.out.println("✓ Connecting to PostgreSQL: " + host + ":" + port + "/" + database);
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000); // 30 seconds
        config.setIdleTimeout(600000); // 10 minutes
        config.setMaxLifetime(1800000); // 30 minutes
        
        return new HikariDataSource(config);
    }
}
