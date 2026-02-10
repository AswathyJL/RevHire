package com.pack.RevHire.view;

import com.pack.RevHire.model.*;
import com.pack.RevHire.service.*;
import java.util.*;

public class JobSearchView {
    private Scanner scanner = new Scanner(System.in);
    private JobSearchService searchService = new JobSearchService();
    private JobService jobService = new JobService();
    private SkillService skillService = new SkillService();
    private JobApplicationService appService = new JobApplicationService();

    public void displayMenu(User user) {
        while (true) {
            System.out.println("\n--- JOB SEARCH ---");
            System.out.println("1. View All Jobs");
            System.out.println("2. Search by Title");
            System.out.println("3. Search by Skills");
            System.out.println("4. Advanced Filter Search (Role, Loc, Exp, Company, Sal, Type)");
            System.out.println("0. Back");
            System.out.print("Select an option: ");
            
            String choice = scanner.nextLine();
            if (choice.equals("0")) break;

            switch (choice) {
                case "1" -> showJobsTabular(searchService.getAllJobs(), user);
                case "2" -> {
                    System.out.print("Enter Title keyword: ");
                    showJobsTabular(searchService.searchByTitle(scanner.nextLine()), user);
                }
                case "3" -> handleSkillSearch(user);
                case "4" -> handleAdvancedSearch(user);
                default -> System.out.println(">>> Invalid choice.");
            }
        }
    }

    private void handleAdvancedSearch(User user) {
        System.out.println("\n--- ADVANCED FILTERS (Press Enter to skip any field) ---");
        
        System.out.print("Job Role/Title: ");
        String role = scanner.nextLine().trim();
        
        System.out.print("Location: ");
        String loc = scanner.nextLine().trim();
        
        System.out.print("Max Experience Required (Years): ");
        String expIn = scanner.nextLine().trim();
        Integer exp = expIn.isEmpty() ? null : Integer.parseInt(expIn);
        
        System.out.print("Company Name: ");
        String comp = scanner.nextLine().trim();
        
        System.out.print("Minimum Salary (Max Salary should be >= this): ");
        String salIn = scanner.nextLine().trim();
        Double sal = salIn.isEmpty() ? null : Double.parseDouble(salIn);
        
        System.out.print("Job Type (FULL_TIME, PART_TIME, INTERNSHIP, CONTRACT): ");
        String type = scanner.nextLine().trim().toUpperCase();

        // Calling the search service with all filters
        showJobsTabular(searchService.searchByFilters(role, loc, exp, comp, sal, type), user);
    }

    private void showJobsTabular(List<Job> jobs, User user) {
        if (jobs.isEmpty()) { 
            System.out.println(">>> No jobs found matching your criteria."); 
            return; 
        }

        // Expanded table width to accommodate Company Name
        System.out.println("\n" + "-".repeat(120));
        System.out.printf("%-5s | %-25s | %-20s | %-50s\n", "ID", "TITLE", "COMPANY", "SKILLS");
        System.out.println("-".repeat(120));

        for (Job j : jobs) {
            // j.getCompanyName() will now contain data thanks to the DAO Join
            System.out.printf("%-5d | %-25s | %-20s | %-50s\n", 
                j.getJobId(), 
                j.getTitle(), 
                (j.getCompanyName() != null ? j.getCompanyName() : "N/A"),
                searchService.getFormattedSkills(j.getJobId()));
        }
        System.out.println("-".repeat(120));

        System.out.print("Enter Job ID to view full details (0 to go back): ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            if (id != 0) showJobLineByLine(id, user);
        } catch (Exception e) { 
            System.out.println(">>> Invalid ID format. Please enter a number."); 
        }
    }

    private void showJobLineByLine(int jobId, User user) {
        Job job = jobService.getJobById(jobId);
        if (job == null) return;

        boolean alreadyApplied = searchService.isApplied(user.getUserId(), jobId);

        System.out.println("\n" + "=".repeat(65));
        System.out.println("                JOB SPECIFICATIONS                ");
        System.out.println("=".repeat(65));
        System.out.println("TITLE       : " + job.getTitle());
        System.out.println("COMPANY     : " + (job.getCompanyName() != null ? job.getCompanyName() : "N/A"));
        System.out.println("LOCATION    : " + job.getLocation());
        System.out.println("SALARY      : " + job.getSalaryMin() + " - " + job.getSalaryMax());
        System.out.println("SKILLS      : " + searchService.getFormattedSkills(jobId));
        System.out.println("TYPE        : " + job.getJobType());
        System.out.println("DEADLINE    : " + job.getDeadline());
        System.out.println("APPLICATION : " + (alreadyApplied ? "ALREADY APPLIED" : "NOT APPLIED"));
        System.out.println("-".repeat(65));
        System.out.println("DESCRIPTION : \n" + job.getDescription());
        System.out.println("=".repeat(65));

        if (alreadyApplied) {
            System.out.println("\n>>> You have already applied for this position.");
            System.out.println("Press Enter to go back...");
            scanner.nextLine();
        } else {
            System.out.print("\n[1] Apply for this Job [0] Back: ");
            String choice = scanner.nextLine().trim();
            if (choice.equals("1")) {
                applyToJob(jobId, user);
            }
        }
    }

    private void handleSkillSearch(User user) {
        List<Skill> master = skillService.getAllSkills();
        System.out.println("\n--- AVAILABLE SKILLS ---");
        master.forEach(s -> System.out.print(s.getSkillId() + ":" + s.getSkillName() + "  "));
        
        System.out.print("\n\nEnter Skill IDs separated by commas (e.g. 2,13,16): ");
        try {
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) return;
            
            List<Integer> ids = Arrays.stream(input.split(","))
                                      .map(s -> Integer.parseInt(s.trim()))
                                      .toList();
            showJobsTabular(searchService.searchBySkills(ids), user);
        } catch (Exception e) { 
            System.out.println(">>> Invalid Input. Please use format: ID,ID"); 
        }
    }

    private void applyToJob(int jobId, User user) {
        System.out.println("\n--- SUBMIT YOUR APPLICATION ---");
        System.out.println("Enter your Cover Letter / Professional Pitch:");
        String coverLetter = scanner.nextLine().trim();

        if (coverLetter.isEmpty()) {
            System.out.println(">>> Application cancelled. Cover letter is required.");
            return;
        }

        boolean isSuccess = appService.submitApplication(jobId, user.getUserId(), coverLetter);

        if (isSuccess) {
            System.out.println("\n******************************************");
            System.out.println("SUCCESS: Your application has been submitted!");
            System.out.println("******************************************");
        } else {
            System.out.println(">>> Failed to submit application. Please contact support.");
        }
    }
}