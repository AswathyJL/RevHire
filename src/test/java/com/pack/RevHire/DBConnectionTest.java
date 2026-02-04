package com.pack.RevHire;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

import com.pack.RevHire.config.DBConnection;

class DBConnectionTest 
{

	@Test
	void testGetConnection() throws SQLException
	{
		// 1. Act: Try to get the connection
        Connection connection = DBConnection.getConnection();

        // 2. Assert: Check if the connection is not null
        assertNotNull(connection, "Connection should not be null. Check if db.properties exists in src/main/resources.");

        // 3. Assert: Check if the connection is actually valid/open
        assertTrue(connection.isValid(2), "Connection should be valid and reachable.");
        
        // Clean up
        if (connection != null) {
            connection.close();
        }
	}

}
