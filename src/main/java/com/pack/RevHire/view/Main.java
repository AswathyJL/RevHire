package com.pack.RevHire.view;

import java.util.Scanner;

public class Main{
    private view userConsole = new view();
    private Scanner scanner = new Scanner(System.in);

    public void start() {
        while (true) {
            System.out.println("\n--- REVHIRE MAIN MENU ---");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Forgot Password");
            System.out.println("4. Exit System");
            System.out.println("Please choose enter for the action required: ");

            int choice = scanner.nextInt();
            switch (choice) {
                case 1: userConsole.displayLoginMenu(); break;
                case 2: userConsole.displayRegisterMenu(); break;
                case 3: userConsole.displayForgotPasswordMenu(); break;
                case 4: 
                    System.out.println("Shutting down...");
                    return; // Returns to App.java which then finishes
                default: 
                    System.out.println("Invalid choice.");
            }
        }
    }
}