package com.pack.RevHire.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DatabaseInitializer {
	
//	logger
	private static final Logger logger = LogManager.getLogger(DatabaseInitializer.class);

   public static void runScriptOnce(Connection conn) {
	   logger.info("Starting Database check to verify required tables...");
    try 
//    (Connection conn = DBConnection.getConnection()) 
    {
        // 1. Get ALL tables in your schema in ONE hit
        Set<String> actualTables = new HashSet<>();
        ResultSet rs = conn.getMetaData().getTables(null, "REV_USR", "%", new String[]{"TABLE"});
        while (rs.next()) {
            actualTables.add(rs.getString("TABLE_NAME").toUpperCase());
        }

        // 2. The list of tables your JD requires
        String[] expected = {"USERS", "JOBS", "PROJECTS", "CERTIFICATIONS", "JOB_APPLICATIONS"};
        
        boolean missingSomething = false;
        for (String table : expected) {
            if (!actualTables.contains(table)) {
            	logger.warn("Table missing: {}. Schema initialization will be triggered.", table);
                missingSomething = true;
                break;
            }
        }

        if (missingSomething) {
            executeSQL(conn);
        }
        else
        {
        	logger.info("Database check complete: All required tables already exist.");
        }
    } catch (Exception e) {
    	logger.error("Error during database table verification: {}", e.getMessage(), e);
//        e.printStackTrace();
    }
}

    private static void executeSQL(Connection conn) {
    	logger.info("Executing schema.sql script to build tables...");
        // 2. Read schema.sql from src/main/resources
        try (InputStream is = DatabaseInitializer.class.getClassLoader().getResourceAsStream("schema.sql");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is));
             Statement stmt = conn.createStatement()) {
        	
        	if (is == null) {
                logger.error("Critical Error: schema.sql file not found in resources folder.");
                return;
            }

            String sql = reader.lines().collect(Collectors.joining("\n"));
            
            // 3. Split by semicolon and execute each query
            int count = 0;
            String[] queries = sql.split(";");
            for (String query : queries) {
                if (!query.trim().isEmpty()) {
                    stmt.execute(query);
                    count++;
                }
            }
//            System.out.println("All 14 RevHire tables created successfully!");
            logger.info("Database initialization successful. Total queries executed: {}", count);

        } catch (Exception e) {
//            System.err.println("Error executing schema script: " + e.getMessage());
        	logger.error("Failed to execute schema script: {}", e.getMessage(), e);
        
        }
    }
}