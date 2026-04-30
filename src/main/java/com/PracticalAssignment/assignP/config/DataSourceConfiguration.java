package com.PracticalAssignment.assignP.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.jdbc.DataSourceBuilder;
import javax.sql.DataSource;
import java.net.URI;

/**
 * Configuration to properly parse Heroku's DATABASE_URL environment variable for PostgreSQL.
 * Heroku provides DATABASE_URL in format: postgres://user:password@host:port/dbname
 * This needs to be converted to JDBC format: jdbc:postgresql://host:port/dbname
 * 
 * This bean is only created when the 'postgres' profile is active (via Procfile in Heroku).
 */
@Configuration
@Profile("postgres")
public class DataSourceConfiguration {

    public DataSourceConfiguration() {
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║ DataSourceConfiguration INITIALIZED        ║");
        System.out.println("║ Profile: postgres                          ║");
        System.out.println("╚════════════════════════════════════════════╝\n");
    }

    @Bean
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        
        if (databaseUrl == null || databaseUrl.isEmpty()) {
            System.err.println("\n╔════════════════════════════════════════════╗");
            System.err.println("║ FATAL ERROR                             ║");
            System.err.println("║ DATABASE_URL environment variable is NOT   ║");
            System.err.println("║ set. Heroku PostgreSQL add-on must be     ║");
            System.err.println("║ installed.                                 ║");
            System.err.println("╚════════════════════════════════════════════╝\n");
            throw new IllegalStateException(
                "DATABASE_URL environment variable is not set. " +
                "Heroku PostgreSQL add-on must be installed."
            );
        }

        try {
            System.out.println("Parsing Heroku DATABASE_URL...");
            
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
            
            // Build JDBC URL for PostgreSQL
            String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s?sslmode=require", host, port, database);
            
            System.out.println("\n✓ Successfully parsed DATABASE_URL:");
            System.out.println("  └─ Host: " + host);
            System.out.println("  └─ Port: " + port);
            System.out.println("  └─ Database: " + database);
            System.out.println("  └─ User: " + username);
            System.out.println("  └─ SSL Mode: require\n");
            
            return DataSourceBuilder.create()
                    .driverClassName("org.postgresql.Driver")
                    .url(jdbcUrl)
                    .username(username)
                    .password(password)
                    .build();
                    
        } catch (Exception e) {
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
