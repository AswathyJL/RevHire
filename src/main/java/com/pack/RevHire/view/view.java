package com.pack.RevHire.view;
import com.pack.RevHire.model.User;
import com.pack.RevHire.service.UserService;

import java.io.Console;
import java.util.Scanner;

public class view {
    private UserService userService = new UserService();
    private Scanner scanner = new Scanner(System.in);
    
    private JobSeekerView seekerConsole = new JobSeekerView();
    private EmployerView employerConsole = new EmployerView();

    /**
     * Handles the Registration Flow
     */
    public void displayRegisterMenu() {
        System.out.println("\n--- User Registration ---");
        
        System.out.print("Enter Email: ");
        String email = scanner.nextLine();
        
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();
        
        System.out.print("Enter Role (JOB_SEEKER / EMPLOYER): ");
        String role = scanner.nextLine().toUpperCase();
        
        System.out.print("Set a Security Question: ");
        String question = scanner.nextLine();
        
        System.out.print("Set the Security Answer: ");
        String answer = scanner.nextLine();

        // Creating the User object (Make sure your User model has this constructor)
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setRole(role);
        newUser.setSecurityQuestion(question);
        newUser.setSecurityAnswer(answer);

        String result = userService.registerUser(newUser);
        System.out.println("\n>>> " + result);
    }

    /**
     * Handles the Login Flow
     */
   
    public void displayLoginMenu() 
    {
        System.out.println("\n--- User Login ---");
        
        System.out.print("Email: ");
        String email = scanner.nextLine();
        
//        System.out.print("Password: ");
//        String password = scanner.nextLine();
        String password = ""; // 1. Initialize outside the IF block
        Console cons = System.console();
        
        if(cons!= null) {
        	char[] passwordChars = cons.readPassword("Password: ");
        	password = new String(passwordChars);
            java.util.Arrays.fill(passwordChars, ' ');
        }
        else {
            // 2. Fallback for Eclipse/IDE Console
            System.out.print("Password: ");
            password = scanner.nextLine();
        }
        User loggedInUser = userService.login(email, password);

        if (loggedInUser != null) {
            System.out.println("\nSUCCESS: Welcome " + loggedInUser.getEmail() + " [" + loggedInUser.getRole() + "]");
            // Logic for showing different dashboards based on role would go here
            if (loggedInUser.getRole().equalsIgnoreCase("JOB_SEEKER")) {
            	seekerConsole.displayDashboard(loggedInUser);
            } else {
            	employerConsole.displayDashboard(loggedInUser);
            }
        } else {
            System.out.println("\nERROR: Invalid email or password.");
        }
    }

    /**
     * Handles the Forgot Password Flow (3-Step Process)
     */
    public void displayForgotPasswordMenu() {
        System.out.println("\n--- Forgot Password Recovery ---");
        
        System.out.print("Enter your registered Email: ");
        String email = scanner.nextLine();

        // Step 1: Fetch Question via Service
        String question = userService.getSecurityQuestion(email);
        
        if (question.equals("User not found.") || question.equals("Email cannot be empty.")) {
            System.out.println(">>> " + question);
            return;
        }

        // Step 2: Show Question and get Answer
        System.out.println("Security Question: " + question);
        System.out.print("Your Answer: ");
        String answer = scanner.nextLine();

        // Step 3: Verify Answer and Update Password
        if (userService.verifySecurityAnswer(email, answer)) {
            System.out.println("Verification Successful!");
            System.out.print("Enter New Password: ");
            String newPass = scanner.nextLine();
            
            String result = userService.resetPassword(email, newPass);
            System.out.println(">>> " + result);
        } else {
            System.out.println(">>> Verification Failed: Incorrect answer.");
        }
    }
}