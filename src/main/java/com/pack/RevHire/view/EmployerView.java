package com.pack.RevHire.view;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.pack.RevHire.dao.NotificationDAO;
import com.pack.RevHire.dao.implementation.NotificationDAOImpl;
import com.pack.RevHire.model.EmployerProfile;
import com.pack.RevHire.model.JobSeekerProfile;
import com.pack.RevHire.model.Notification;
import com.pack.RevHire.model.Skill;
import com.pack.RevHire.model.User;
import com.pack.RevHire.service.JobApplicationService;
//import com.pack.RevHire.service.JobSearchService;
import com.pack.RevHire.service.ResumeService;
import com.pack.RevHire.service.SkillService;
import com.pack.RevHire.service.UserService;

public class EmployerView 
{
	private UserService userService = new UserService();
	private JobView jobView = new JobView();
	private JobApplicationService appService = new JobApplicationService();
    private SkillService skillService = new SkillService();
//    private JobSearchService jobSearchSerivce = new JobSearchService();
    private ResumeService resumeService = new ResumeService();
    public NotificationDAO notificationDAO = new NotificationDAOImpl();
	private Scanner scanner = new Scanner(System.in);
	

    public void displayDashboard(User user) {
        while (true) {
        	System.out.println("\n===== EMPLOYER DASHBOARD =====");
            System.out.println("1. Manage Company Information");
            System.out.println("2. Post a New Job Opening");
            System.out.println("3. Manage Job Postings (Edit/Close/Stats)");
            System.out.println("4. Search & Filter Applicants");
            System.out.println("5. Notifications");
            System.out.println("6. Change Password");
            System.out.println("7. Logout");
            System.out.print("Select activity: ");

            int choice ;

            try {
                // .trim() removes any accidental tabs or spaces
                choice = Integer.parseInt(scanner.nextLine().trim()); 
            } catch (NumberFormatException e) {
                System.out.println(">>> ERROR: Please enter a valid number (1-6).");
                continue; // This skips the rest of the loop and starts over
            }

            if (choice == 7) {
                System.out.println("Logging out...");
                break;
            }
            switch (choice) 
            {
            case 1: manageEmployerProfile(user);
            		break;
            case 2: jobView.postJobFlow(user);
            		break;
            case 3: jobView.manageJobPostings(user);
            		break;
            case 4: filterApplicantsFlow(user);
            		break;
            case 5: notificationFlow(user);
    				break;
    				
            case 6: changePasswordFlow(user);
					break;
            case 7: System.out.println("logout");
            		break;
            default: System.out.println("Invalid choice.");
        }
        }
    }
    
    private void manageEmployerProfile(User user) {
	    // Attempt to fetch the profile
	    EmployerProfile profile = userService.getEmployerProfile(user.getUserId());
	
	    if (profile != null) {
	        // SCENARIO 1: Profile Exists - Display then ask to Edit
	        System.out.println("\n--- Current Company Profile ---");
	        System.out.println("Name:        " + profile.getCompanyName());
	        System.out.println("Industry:    " + profile.getIndustry());
	        System.out.println("Size:        " + profile.getCompanySize());
	        System.out.println("Description: " + profile.getDescription());
	        System.out.println("Website:     " + profile.getWebsite());
	        System.out.println("Location:    " + profile.getLocation());
	        System.out.println("-------------------------------");
	
	        System.out.print("\nDo you want to edit this information? (yes/no): ");
	        if (scanner.nextLine().equalsIgnoreCase("yes")) {
	            updateProfileFlow(user, profile); // Pass existing profile to keep values
	        }
	    } else {
	        // SCENARIO 2: Profile is Missing - Ask to Create
	        System.out.println("\n[!] No company profile found for your account.");
	        System.out.print("Would you like to create one now? (yes/no): ");
	        if (scanner.nextLine().equalsIgnoreCase("yes")) {
	            updateProfileFlow(user, null); // Pass null because it's a new creation
	        }
	    }
	}
    
    private void updateProfileFlow(User user, EmployerProfile existing) {
        EmployerProfile newProfile = new EmployerProfile();
        newProfile.setUserId(user.getUserId());

        System.out.println("\n--- Updating Details (Press ENTER to keep current value) ---");

        // Helper logic: If existing is null, we show "None", otherwise show the current value
        
        // 1. Company Name
        System.out.print("Company Name [" + (existing != null ? existing.getCompanyName() : "None") + "]: ");
        String input = scanner.nextLine();
        newProfile.setCompanyName(input.isEmpty() && existing != null ? existing.getCompanyName() : input);

        // 2. Industry
        System.out.print("Industry [" + (existing != null ? existing.getIndustry() : "None") + "]: ");
        input = scanner.nextLine();
        newProfile.setIndustry(input.isEmpty() && existing != null ? existing.getIndustry() : input);

        // 3. Size
        System.out.print("Company Size [" + (existing != null ? existing.getCompanySize() : "None") + "]: ");
        input = scanner.nextLine();
        newProfile.setCompanySize(input.isEmpty() && existing != null ? existing.getCompanySize() : input);

        // 4. Description
        System.out.print("Description [" + (existing != null ? existing.getDescription() : "None") + "]: ");
        input = scanner.nextLine();
        newProfile.setDescription(input.isEmpty() && existing != null ? existing.getDescription() : input);

        // 5. Website
        System.out.print("Website [" + (existing != null ? existing.getWebsite() : "None") + "]: ");
        input = scanner.nextLine();
        newProfile.setWebsite(input.isEmpty() && existing != null ? existing.getWebsite() : input);

        // 6. Location
        System.out.print("Location [" + (existing != null ? existing.getLocation() : "None") + "]: ");
        input = scanner.nextLine();
        newProfile.setLocation(input.isEmpty() && existing != null ? existing.getLocation() : input);

        // Save to database
        if (userService.saveEmployerProfile(newProfile)) {
            System.out.println("\n>>> SUCCESS: Profile saved!");
        } else {
            System.out.println("\n>>> ERROR: Could not save profile.");
        }
    }
    
    private void changePasswordFlow(User user) {
        System.out.println("\n--- Change Password ---");
        System.out.print("Enter Current Password: ");
        String oldPass = scanner.nextLine();
        
        System.out.print("Enter New Password: ");
        String newPass = scanner.nextLine();
        
        System.out.print("Confirm New Password: ");
        String confirmPass = scanner.nextLine();

        if (!newPass.equals(confirmPass)) {
            System.out.println(">>> ERROR: New passwords do not match!");
            return;
        }
        else if (newPass.length() < 6)
        {
        	System.out.println(">>> ERROR: New Password should have at least 6 characters!");
            return;
        }

        // Call service to validate and update
        if (userService.updateUserPassword(user.getUserId(), oldPass, newPass)) {
            user.setPassword(newPass); // Update the current session object
            System.out.println(">>> SUCCESS: Password updated successfully!");
        } else {
            System.out.println(">>> ERROR: Incorrect current password or system error.");
        }
    }
    
private void filterApplicantsFlow(User user) {
        while (true) {
            System.out.println("\n--- APPLICANT MANAGEMENT ---");
            System.out.println("1. View Applicants by Job Posting");
            System.out.println("2. Global Skill-Based Search (All Applicants)");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("0")) break;

            switch (choice) {
                case "1" -> showJobsWithApplicantCount(user);
                case "2" -> globalSkillSearch();
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void showJobsWithApplicantCount(User user) {
        List<Map<String, Object>> jobs = appService.getJobStats(user.getUserId());
        if (jobs.isEmpty()) {
            System.out.println(">>> You haven't posted any jobs yet.");
            return;
        }

        System.out.println("\n--- YOUR JOB POSTINGS & STATS ---");
        System.out.printf("%-10s | %-30s | %-10s\n", "JOB ID", "TITLE", "APPLICANTS");
        System.out.println("-".repeat(55));
        for (Map<String, Object> j : jobs) {
            System.out.printf("%-10s | %-30.30s | %-10s\n", j.get("job_id"), j.get("title"), j.get("applicant_count"));
        }

        System.out.print("\nEnter Job ID to view applicants (0 to back): ");
        try {
            int jobId = Integer.parseInt(scanner.nextLine());
            if (jobId != 0) showApplicantsForJob(jobId);
        } catch (Exception e) { System.out.println("Invalid Input."); }
    }

    private void showApplicantsForJob(int jobId) {
        List<Map<String, Object>> applicants = appService.getApplicantsForJob(jobId);
        if (applicants.isEmpty()) {
            System.out.println(">>> No one has applied for this job yet.");
            return;
        }

        System.out.println("\n--- APPLICANTS FOR JOB ID: " + jobId + " ---");
        System.out.printf("%-10s | %-25s | %-12s | %-12s\n", "APP ID", "NAME", "STATUS", "APPLIED ON");
        System.out.println("-".repeat(65));
        for (Map<String, Object> a : applicants) {
            System.out.printf("%-10s | %-25s | %-12s | %-12s\n", 
                a.get("application_id"), a.get("name"), a.get("status"), a.get("applied_at").toString().substring(0,10));
        }

        System.out.print("\nOptions: [1] View Profile [2] Change Status [3] Skill Filter [0] Back: ");
        String choice = scanner.nextLine().trim();
        if (choice.equals("1")) handleViewProfile(applicants);
        else if (choice.equals("2")) {
            System.out.print("Enter Application ID: ");
            try {
                int appId = Integer.parseInt(scanner.nextLine());
                handleStatusUpdate(appId);
            } catch (Exception e) { System.out.println("Invalid ID."); }
        }
        else if (choice.equals("3")) skillFilterSubFlow(jobId);
    }

    // inside EmployerView.java

	private void handleViewProfile(List<Map<String, Object>> applicants) {
	    System.out.print("\nEnter Application ID to view profile (0 to back): ");
	    try {
	        String inputId = scanner.nextLine().trim();
	        if (inputId.equals("0")) return;
	
	        Map<String, Object> selected = applicants.stream()
	            .filter(a -> a.get("application_id").toString().equals(inputId))
	            .findFirst().orElse(null);
	
	        if (selected == null) {
	            System.out.println(">>> Error: Application ID not found.");
	            return;
	        }
	
	        int userId = Integer.parseInt(selected.get("user_id").toString());
	        int appId = Integer.parseInt(selected.get("application_id").toString());
	        
	        // Use your service to get the Profile
	        JobSeekerProfile profile = userService.getJobSeekerProfile(userId);
	
	        if (profile != null) {
	            // Use your service to get or create the Resume ID
	            int resumeId = resumeService.getOrCreateResumeId(userId);
	            String email = userService.getUserEmail(userId); // Assuming this exists to get contact email
	
	            System.out.println("\n" + "=".repeat(50));
	            System.out.println("               CANDIDATE DOSSIER               ");
	            System.out.println("=".repeat(50));
	            System.out.println("NAME             : " + profile.getFullName());
	            System.out.println("PHONE            : " + profile.getPhone());
	            System.out.println("LOCATION         : " + profile.getLocation());
	            System.out.println("TOTAL EXPERIENCE : " + profile.getTotalExperience() + " Years");
	            System.out.println("=".repeat(50));
	
	            System.out.println("\nActions: [1] View Full Resume [2] Change Status [0] Back");
	            String choice = scanner.nextLine().trim();
	
	            if (choice.equals("1")) {
	                // Now call the view from ResumeView passing your service's data
	                ResumeView rv = new ResumeView();
	                rv.viewReadOnlyResume(resumeId, profile, email);
	            } else if (choice.equals("2")) {
	                handleStatusUpdate(appId);
	            }
	        }
	    } catch (Exception e) {
	        System.out.println(">>> Error: " + e.getMessage());
	    }
	}

    private void handleStatusUpdate(int appId) {
        System.out.println("\n--- UPDATE APPLICATION STATUS ---");
        System.out.println("1. SHORTLISTED");
        System.out.println("2. REJECTED");
        System.out.print("Select Status: ");
        String sChoice = scanner.nextLine().trim();
        
        String status = sChoice.equals("1") ? "SHORTLISTED" : "REJECTED";
        System.out.print("Enter Comments for the Applicant: ");
        String comments = scanner.nextLine().trim();

        if (appService.updateApplicationByEmployer(appId, status, comments)) {
            System.out.println("\n>>> SUCCESS: Applicant marked as " + status);
        } else {
            System.out.println(">>> ERROR: Could not update status.");
        }
    }

    private void skillFilterSubFlow(int jobId) {
        System.out.println("\n--- FILTER APPLICANTS BY SKILLS (Job ID: " + jobId + ") ---");
        List<Skill> master = skillService.getAllSkills();
        master.forEach(s -> System.out.print("[" + s.getSkillId() + "]" + s.getSkillName() + "  "));
        
        System.out.print("\n\nSelect Skill IDs (comma-separated): ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) return;

        List<Integer> skillIds = Arrays.stream(input.split(","))
                                       .map(s -> Integer.parseInt(s.trim()))
                                       .collect(Collectors.toList());

        List<Map<String, Object>> filtered = appService.getApplicantsBySkills(jobId, skillIds);
        if (filtered.isEmpty()) {
            System.out.println(">>> No applicants for this job match these skills.");
        } else {
            handleViewProfile(filtered); // Reuse view profile for the filtered list
        }
    }

    private void globalSkillSearch() {
        System.out.println("\n--- GLOBAL TALENT SEARCH ---");
        List<Skill> master = skillService.getAllSkills();
        master.forEach(s -> System.out.print("[" + s.getSkillId() + "]" + s.getSkillName() + "  "));
        
        System.out.print("\n\nEnter Skill IDs to find candidates: ");
        try {
            String input = scanner.nextLine().trim();
            List<Integer> skillIds = Arrays.stream(input.split(","))
                                           .map(s -> Integer.parseInt(s.trim()))
                                           .collect(Collectors.toList());

            List<Map<String, Object>> candidates = appService.searchCandidatesBySkills(skillIds);
            
            if (candidates.isEmpty()) {
                System.out.println(">>> No matching candidates found.");
            } else {
                System.out.println("\n--- MATCHING CANDIDATES ---");
                System.out.printf("%-10s | %-25s | %-20s\n", "USER ID", "NAME", "LOCATION");
                for (Map<String, Object> c : candidates) {
                    System.out.printf("%-10s | %-25s | %-20s\n", c.get("user_id"), c.get("name"), c.get("location"));
                }
                // Option to view profile would go here
            }
        } catch (Exception e) { System.out.println("Invalid Input."); }
    }
    
//    notifications
	  public void notificationFlow(User user) {
	    while (true) {
	        List<Notification> list = notificationDAO.getNotificationsByUserId(user.getUserId());
	        
	        // 1. Sort: Newest first, then read status
	        list.sort((n1, n2) -> {
	            if (n1.isRead() != n2.isRead()) {
	                return Boolean.compare(n1.isRead(), n2.isRead());
	            }
	            return n2.getCreatedAt().compareTo(n1.getCreatedAt());
	        });
	
	        System.out.println("\n" + "=".repeat(80));
	        System.out.println("                                NOTIFICATIONS");
	        System.out.println("=".repeat(80));
	
	        if (list.isEmpty()) {
	            System.out.println("                    >>> You have no notifications.");
	            System.out.println("=".repeat(80));
	            System.out.println("\nOptions: [0] Back");
	        } else {
	            System.out.printf("%-5s | %-10s | %-15s | %-40s\n", "ID", "STATUS", "DATE", "MESSAGE");
	            System.out.println("-".repeat(80));
	            for (Notification n : list) {
	                String status = n.isRead() ? "[READ]" : "[NEW!]";
	                System.out.printf("%-5d | %-10s | %-15s | %-40s\n", 
	                    n.getNotificationId(), status, 
	                    n.getCreatedAt().toString().substring(0, 10), n.getMessage());
	            }
	            System.out.println("=".repeat(80));
	            System.out.println("\nOptions: [1] Mark Read [2] Delete [0] Back");
	            System.out.println("Tip: Enter multiple IDs separated by commas (e.g., 101,104,105)");
	        }
	
	        System.out.print("Action: ");
	        String action = scanner.nextLine().trim();
	
	        if (action.equals("0")) break;
	        
	        // 2. Prevent actions if no notifications exist
	        if (list.isEmpty() && (action.equals("1") || action.equals("2"))) {
	            System.out.println(">>> Error: No notifications available to process.");
	            continue;
	        }
	
	        if (action.equals("1") || action.equals("2")) {
	            System.out.print("Enter Notification ID(s): ");
	            String input = scanner.nextLine().trim();
	            
	            try {
	                List<Integer> targetIds = Arrays.stream(input.split(","))
	                                                .map(String::trim)
	                                                .map(Integer::parseInt)
	                                                .collect(Collectors.toList());
	
	                // 3. Validation: Check if IDs actually belong to the displayed list
	                List<Integer> validIdsInList = list.stream()
	                                                   .map(Notification::getNotificationId)
	                                                   .collect(Collectors.toList());
	                
	                if (!validIdsInList.containsAll(targetIds)) {
	                    System.out.println(">>> Error: One or more IDs are invalid or do not belong to you.");
	                    continue;
	                }
	
	                if (action.equals("1")) {
	                    notificationDAO.bulkMarkAsRead(targetIds);
	                    System.out.println(">>> Selected notifications marked as read.");
	                } else {
	                    notificationDAO.bulkDelete(targetIds);
	                    System.out.println(">>> Selected notifications deleted.");
	                }
	            } catch (NumberFormatException e) {
	                System.out.println(">>> Error: Please enter numeric IDs separated by commas.");
	            }
	        }
		    }
		}
	    
}
