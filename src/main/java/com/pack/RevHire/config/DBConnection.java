package com.pack.RevHire.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DBConnection {
	
//	logger
	private static final Logger logger = LogManager.getLogger(DBConnection.class);
	
	public static Connection getConnection() {
        Connection conn = null;
        try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            Properties prop = new Properties();

            if (input == null) {
            	logger.error("Error: db.properties not found in src/main/resources");
                return null;
            }

            prop.load(input);

            // Fetching secrets from the properties file
            String url = prop.getProperty("db.url");
            String user = prop.getProperty("db.user");
            String pass = prop.getProperty("db.password");

            conn = DriverManager.getConnection(url, user, pass);

         // Log successful connection at INFO level
            logger.info("Successfully established connection to database: {}", url);
            
        } catch (Exception e) {
        	logger.error("Database connection failed: {}", e.getMessage(), e);
        }
        return conn;
    }
}
