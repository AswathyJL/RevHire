package com.pack.RevHire.view;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.pack.RevHire.model.Job;
import com.pack.RevHire.model.User;
import com.pack.RevHire.service.JobApplicationService;
import com.pack.RevHire.service.JobSearchService;
import com.pack.RevHire.service.JobService;

public class JobApplicationView {

    private Scanner scanner = new Scanner(System.in);
    private JobApplicationService appService = new JobApplicationService();
    private JobService jobService = new JobService();
    private JobSearchService jobSearchService = new JobSearchService(); // Reusing for formatting

    public void displayDashboard(User user) {
        while (true) {
            List<Map<String, Object>> apps = appService.getMyApplications(user.getUserId());
            
            System.out.println("\n" + "=".repeat(110));
            System.out.println("                                MY JOB APPLICATIONS                                ");
            System.out.println("=".repeat(110));
//            System.out.printf("%-8s | %-8s | %-25s | %-12s | %-20s | %-12s\n", 
//                              "APP ID", "JOB ID", "JOB TITLE", "STATUS", "EMPLOYER COMMENTS", "APPLIED ON");
            System.out.printf("%-8s | %-8s | %-25s | %-12s | %-12s\n", 
                    "APP ID", "JOB ID", "JOB TITLE", "STATUS", "APPLIED ON");
            System.out.println("-".repeat(110));

            if (apps.isEmpty()) {
                System.out.println("                                No applications found.                             ");
            } else {
                for (Map<String, Object> a : apps) {
				    // Check your SQL query: if you didn't use an alias, 
				    // it's "application_id" and "job_id"
				    Object appIdObj = a.get("application_id"); 
				    Object jobIdObj = a.get("job_id");
				    
				    String dateStr = a.get("applied_at") != null ? a.get("applied_at").toString().substring(0, 10) : "N/A";
				    
//				    System.out.printf("%-8s | %-8s | %-25.25s | %-12s | %-20.20s | %-12s\n",
//				        appIdObj, 
//				        jobIdObj, 
//				        a.get("title"), 
//				        a.get("status"), 
//				        (a.get("employer_comments") == null ? "N/A" : a.get("employer_comments")),
//				        dateStr);
				    System.out.printf("%-8s | %-8s | %-25.25s | %-12s | %-12s\n",
		                    appIdObj, 
		                    jobIdObj, 
		                    a.get("title"), 
		                    a.get("status"), 
		                    dateStr);
				}
            }
            System.out.println("=".repeat(110));

            System.out.println("\nOptions: [1] View Job [2] Withdraw App [3] Delete (Rejected/Withdrawn) [0] Back");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("0")) break;
            
            if (!choice.equals("1") && !choice.equals("2") && !choice.equals("3")) {
                System.out.println(">>> Invalid option. Please select 1, 2, or 3.");
                continue; 
            }
            
            // Pass the user object here to fix the "cannot be resolved" error
            handleChoice(choice, apps, user);
        }
    }

	    private void handleChoice(String choice, List<Map<String, Object>> apps, User user) {
	    System.out.print("Enter Application ID: ");
	    try {
	        String inputId = scanner.nextLine().trim();
	        
	        // Find map by comparing against "application_id" (lowercase key from DAO)
	        Map<String, Object> selected = apps.stream()
	            .filter(a -> a.get("application_id") != null && 
	                         a.get("application_id").toString().equals(inputId))
	            .findFirst().orElse(null);
	
	        if (selected == null) {
	            System.out.println(">>> Invalid Application ID. Please select an ID from the table above.");
	            return;
	        }
	
	        // Extract values using correct keys
	        int appId = Integer.parseInt(selected.get("application_id").toString());
	        int jobId = Integer.parseInt(selected.get("job_id").toString());
	        String status = String.valueOf(selected.get("status"));
	
	        switch (choice) {
	            case "1" -> viewJobDetails(jobId, user);
	            case "2" -> withdrawFlow(appId, status);
	            case "3" -> deleteFlow(appId, status);
	            default -> System.out.println(">>> Invalid option.");
	        }
	    } catch (NumberFormatException e) {
	        System.out.println(">>> Error: Please enter a numeric ID.");
	    } catch (Exception e) { 
	        System.out.println(">>> Error: An unexpected error occurred."); 
	    }
	}

    private void viewJobDetails(int jobId, User user) {
        Job job = jobService.getJobById(jobId);
        if (job == null) {
            System.out.println(">>> Error: Could not retrieve job details.");
            return;
        }

        // Using jobSearchService to get formatted skills
        String skills = jobSearchService.getFormattedSkills(jobId);

        System.out.println("\n" + "*".repeat(20) + " JOB SPECIFICATIONS " + "*".repeat(20));
        System.out.println("TITLE       : " + job.getTitle());
        System.out.println("LOCATION    : " + job.getLocation());
        System.out.println("TYPE        : " + job.getJobType());
        System.out.println("SALARY      : " + job.getSalaryMin() + " - " + job.getSalaryMax());
        System.out.println("EXPERIENCE  : " + job.getExperienceRequired() + " years");
        System.out.println("SKILLS      : " + skills);
        System.out.println("DEADLINE    : " + job.getDeadline());
        System.out.println("-".repeat(60));
        System.out.println("DESCRIPTION : \n" + job.getDescription());
        System.out.println("*".repeat(60));

        System.out.println("\nPress Enter to return to your applications...");
        scanner.nextLine();
    }

    private void withdrawFlow(int appId, String status) {
        if (status.equals("REJECTED") || status.equals("WITHDRAWN")) {
            System.out.println(">>> You cannot withdraw an application that is already " + status);
            return;
        }
        System.out.print("Enter reason for withdrawal: ");
        String reason = scanner.nextLine();
        if (appService.withdrawApplication(appId, reason)) {
            System.out.println(">>> Application withdrawn successfully.");
        }
    }

    private void deleteFlow(int appId, String status) {
        if (status.equals("REJECTED") || status.equals("WITHDRAWN")) {
            System.out.print("Are you sure you want to delete this record? (y/n): ");
            if (scanner.nextLine().equalsIgnoreCase("y")) {
                if (appService.deleteApplication(appId)) {
                    System.out.println(">>> Record deleted.");
                }
            }
        } else {
            System.out.println(">>> You can only delete Rejected or Withdrawn applications.");
        }
    }
}