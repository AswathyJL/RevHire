package com.pack.RevHire;

import java.sql.Connection;

import com.pack.RevHire.config.DBConnection;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        System.out.println("Welcome to RevHire2");
        try
        {
        	  Connection connection = DBConnection.getConnection();
        	  if (connection != null) 
        	  {
        	        System.out.println("Successfully connected to the database!");
        	    } else 
        	    {
        	        System.out.println("Database connection failed! Please verify the credentials."); 
        	    }
        }
        catch(Exception e)
        {
        	  System.out.println("Database connection failed!");
              e.printStackTrace();
        }
    }
}
