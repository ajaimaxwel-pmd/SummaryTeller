package org.theMaxwellGlobe;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DatabaseConnectionPool {
    private static HikariDataSource dataSource;

    static {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream("src/main/resources/database.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("jdbcUrl"));
        config.setUsername(props.getProperty("username"));
        config.setPassword(props.getProperty("password"));

        dataSource = new HikariDataSource(config);
    }

    public static HikariDataSource getDataSource() {
        return dataSource;
    }
}
