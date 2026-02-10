package com.pack.RevHire;

import java.sql.Connection;

import com.pack.RevHire.config.DBConnection;
import com.pack.RevHire.config.DatabaseInitializer;
import com.pack.RevHire.view.Main;

public class App {
    public static void main(String[] args) {
        System.out.println("Welcome to RevHire");
        System.out.println("*********************");
        
       
        try
        {
        	  Connection connection = DBConnection.getConnection();
        	  if (connection != null) 
        	  {
        		  	// Run Database Setup first
        	        // checking if all 14 tables exist and create them if missing.
        		  	// passing the connection to the initializer
        	        DatabaseInitializer.runScriptOnce(connection);
        	        System.out.println("Successfully connected to the database!");
        	        // calling the main menu
        	        Main gatekeeper = new Main();
        	        gatekeeper.start();
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
