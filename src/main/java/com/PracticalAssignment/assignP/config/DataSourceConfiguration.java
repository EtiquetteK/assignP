package com.PracticalAssignment.assignP.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.jdbc.DataSourceBuilder;
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
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        
        if (databaseUrl == null || databaseUrl.isEmpty()) {
            System.err.println("ERROR: DATABASE_URL environment variable is not set!");
            throw new IllegalStateException("DATABASE_URL environment variable is not set. " +
                    "Heroku PostgreSQL add-on must be installed and DATABASE_URL must be configured.");
        }

        try {
            // Parse DATABASE_URL (format: postgres://user:password@host:port/dbname or postgresql://...)
            String normalizedUrl = databaseUrl.replaceFirst("postgres://", "postgresql://");
            URI dbUri = new URI(normalizedUrl);
            
            String username = dbUri.getUserInfo().split(":")[0];
            String password = dbUri.getUserInfo().split(":")[1];
            String host = dbUri.getHost();
            int port = dbUri.getPort() != -1 ? dbUri.getPort() : 5432;
            String database = dbUri.getPath().substring(1); // Remove leading slash
            
            // Build JDBC URL for PostgreSQL
            String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
            
            System.out.println("✓ Connecting to PostgreSQL at: " + host + ":" + port + "/" + database);
            
            return DataSourceBuilder.create()
                    .driverClassName("org.postgresql.Driver")
                    .url(jdbcUrl)
                    .username(username)
                    .password(password)
                    .build();
                    
        } catch (Exception e) {
            System.err.println("ERROR parsing DATABASE_URL: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalStateException("Failed to parse DATABASE_URL: " + e.getMessage(), e);
        }
    }
}
