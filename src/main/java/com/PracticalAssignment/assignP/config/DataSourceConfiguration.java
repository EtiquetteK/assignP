package com.PracticalAssignment.assignP.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.jdbc.DataSourceBuilder;
import javax.sql.DataSource;
import java.net.URI;

/**
 * Configuration to intelligently detect and configure the datasource.
 * 
 * - If DATABASE_URL is set (Heroku): Parse and use PostgreSQL
 * - Otherwise: Use default MySQL configuration from application-local.properties
 * 
 * This works without relying on Spring profiles, which can be unreliable during startup.
 */
@Configuration
public class DataSourceConfiguration {

    public DataSourceConfiguration() {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            System.out.println("\n╔════════════════════════════════════════════╗");
            System.out.println("║ 🔍 Heroku PostgreSQL Database Detected     ║");
            System.out.println("╚════════════════════════════════════════════╝\n");
        } else {
            System.out.println("\n╔════════════════════════════════════════════╗");
            System.out.println("║ 📦 Using default database configuration   ║");
            System.out.println("╚════════════════════════════════════════════╝\n");
        }
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        
        // If DATABASE_URL is set, we're on Heroku - configure PostgreSQL
        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            return configureHerokuPostgres(databaseUrl);
        }
        
        // Otherwise use default configuration (will be loaded from properties)
        System.out.println("✓ Using application properties for datasource configuration");
        return DataSourceBuilder.create().build();
    }

    private DataSource configureHerokuPostgres(String databaseUrl) {
        try {
            System.out.println("✓ Parsing Heroku DATABASE_URL for PostgreSQL...");
            
            // Parse DATABASE_URL (format: postgres://user:password@host:port/dbname or postgresql://...)
            String normalizedUrl = databaseUrl.replaceFirst("^postgres://", "postgresql://");
            URI dbUri = new URI(normalizedUrl);
            
            String userInfo = dbUri.getUserInfo();
            if (userInfo == null) {
                throw new IllegalArgumentException("DATABASE_URL missing user info");
            }
            
            String[] credentials = userInfo.split(":", 2);
            if (credentials.length != 2) {
                throw new IllegalArgumentException("DATABASE_URL user info must be in format: user:password");
            }
            
            String username = credentials[0];
            String password = credentials[1];
            String host = dbUri.getHost();
            int port = dbUri.getPort() != -1 ? dbUri.getPort() : 5432;
            String database = dbUri.getPath();
            
            if (database.startsWith("/")) {
                database = database.substring(1);
            }
            
            if (database.isEmpty()) {
                throw new IllegalArgumentException("DATABASE_URL missing database name");
            }
            
            // Build JDBC URL for PostgreSQL with SSL
            String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s?sslmode=require", host, port, database);
            
            System.out.println("  ✓ Host: " + host);
            System.out.println("  ✓ Port: " + port);
            System.out.println("  ✓ Database: " + database);
            System.out.println("  ✓ User: " + username);
            System.out.println("  ✓ SSL Mode: require\n");
            
            return DataSourceBuilder.create()
                    .driverClassName("org.postgresql.Driver")
                    .url(jdbcUrl)
                    .username(username)
                    .password(password)
                    .build();
                    
        } catch (Exception e) {
            System.err.println("\n╔════════════════════════════════════════════╗");
            System.err.println("║ ❌ ERROR parsing DATABASE_URL              ║");
            System.err.println("╚════════════════════════════════════════════╝");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.err.println();
            throw new IllegalStateException("Failed to parse DATABASE_URL: " + e.getMessage(), e);
        }
    }
}
            System.err.println("\n╔════════════════════════════════════════════╗");
            System.err.println("║  ERROR parsing DATABASE_URL              ║");
            System.err.println("╚════════════════════════════════════════════╝");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.err.println();
            throw new IllegalStateException("Failed to parse DATABASE_URL: " + e.getMessage(), e);
        }
    }
}
