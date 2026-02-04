package com.pack.RevHire.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DBConnection {
	public static Connection getConnection() {
        Connection conn = null;
        try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            Properties prop = new Properties();

            if (input == null) {
                System.out.println("Error: db.properties not found in src/main/resources");
                return null;
            }

            prop.load(input);

            // Fetching secrets from the properties file
            String url = prop.getProperty("db.url");
            String user = prop.getProperty("db.user");
            String pass = prop.getProperty("db.password");

            conn = DriverManager.getConnection(url, user, pass);
            System.out.println("Connected to database successfully using secret key.");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }
}
