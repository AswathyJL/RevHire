package com.pack.RevHire.view;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.pack.RevHire.dao.NotificationDAO;
import com.pack.RevHire.dao.implementation.NotificationDAOImpl;
import com.pack.RevHire.model.JobSeekerProfile;
import com.pack.RevHire.model.Notification;
import com.pack.RevHire.model.User;
import com.pack.RevHire.service.UserService;

public class JobSeekerView 
{
	private UserService userService = new UserService();
	private ResumeView resumeView = new ResumeView();
	private Scanner scanner = new Scanner(System.in);
	private JobSearchView jobSearchView = new JobSearchView();
	private JobApplicationView jobApplicationView = new JobApplicationView();
	private NotificationDAO notificationDAO = new NotificationDAOImpl();
	
	public void displayDashboard(User user) 
	{
	    while (true) 
	    {	
	        System.out.println("\n===== JOB SEEKER DASHBOARD =====");
	        System.out.println("1. Manage Profile (Personal Details)");
	        System.out.println("2. Build/Edit Resume (Education, Experience, Skills)");
	        System.out.println("3. Search & Apply for Jobs");
	        System.out.println("4. View My Applications & Status");
	        System.out.println("5. Notifications");
	        System.out.println("6. Change Password");
	        System.out.println("7. Logout");
	        System.out.print("Select activity: ");
	
	        int choice = Integer.parseInt(scanner.nextLine());
            if (choice == 7) {
                System.out.println("Logging out...");
                break;
            }
	       
	        switch (choice) 
	        {
            case 1: manageJobSeekerProfile(user);
            		break;
            case 2: resumeView.buildResumeMenu(user);
            		break; // Leads to sub-menu for sections
            case 3:  jobSearchView.displayMenu(user);
            		break;
            case 4:jobApplicationView.displayDashboard(user);
            		break;
            case 5: showNotificationsFlow(user);
                	break;
            case 6:changePasswordFlow(user);
    				break;
            case 7:System.out.println("logout"); 
    				break;
            default: System.out.println("Invalid choice.");
	        }
	    }
	}
	
	private void manageJobSeekerProfile(User user) {
	    JobSeekerProfile profile = userService.getJobSeekerProfile(user.getUserId());

	    if (profile != null) {
	        System.out.println("\n--- Current Personal Profile ---");
	        System.out.println("Full Name:  " + profile.getFullName());
	        System.out.println("Phone:      " + profile.getPhone());
	        System.out.println("Location:   " + profile.getLocation());
	        System.out.println("Experience: " + profile.getTotalExperience() + " years");
	        System.out.println("--------------------------------");

	        System.out.print("\nDo you want to edit your profile? (yes/no): ");
	        if (scanner.nextLine().equalsIgnoreCase("yes")) {
	            updateJobSeekerProfileFlow(user, profile);
	        }
	    } else {
	        System.out.println("\n[!] No personal profile found.");
	        System.out.print("Would you like to create your profile now? (yes/no): ");
	        if (scanner.nextLine().equalsIgnoreCase("yes")) {
	            updateJobSeekerProfileFlow(user, null);
	        }
	    }
	}

	private void updateJobSeekerProfileFlow(User user, JobSeekerProfile existing) {
	    JobSeekerProfile newProfile = new JobSeekerProfile();
	    newProfile.setUserId(user.getUserId());

	    System.out.println("\n--- Enter Details (Press ENTER to keep current value) ---");

	    System.out.print("Full Name [" + (existing != null ? existing.getFullName() : "None") + "]: ");
	    String input = scanner.nextLine().trim();
	    newProfile.setFullName(input.isEmpty() && existing != null ? existing.getFullName() : input);

	    System.out.print("Phone [" + (existing != null ? existing.getPhone() : "None") + "]: ");
	    input = scanner.nextLine().trim();
	    newProfile.setPhone(input.isEmpty() && existing != null ? existing.getPhone() : input);

	    System.out.print("Location [" + (existing != null ? existing.getLocation() : "None") + "]: ");
	    input = scanner.nextLine().trim();
	    newProfile.setLocation(input.isEmpty() && existing != null ? existing.getLocation() : input);

	    System.out.print("Years of Experience [" + (existing != null ? existing.getTotalExperience() : "0") + "]: ");
	    input = scanner.nextLine().trim();
	    if (input.isEmpty() && existing != null) {
	        newProfile.setTotalExperience(existing.getTotalExperience());
	    } else {
	        try {
	            newProfile.setTotalExperience(Integer.parseInt(input));
	        } catch (NumberFormatException e) {
	            newProfile.setTotalExperience(existing != null ? existing.getTotalExperience() : 0);
	        }
	    }

	    if (userService.saveJobSeekerProfile(newProfile)) {
	        System.out.println(">>> Profile saved successfully!");
	    } else {
	        System.out.println(">>> ERROR: Failed to save profile.");
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

        // The service already handles the DAO calls we wrote earlier
        if (userService.updateUserPassword(user.getUserId(), oldPass, newPass)) {
            user.setPassword(newPass); 
            System.out.println(">>> SUCCESS: Password updated successfully!");
        } else {
            System.out.println(">>> ERROR: Incorrect current password or system error.");
        }
    }
	
//	notifications
	
	private void showNotificationsFlow(User user) {
	    while (true) {
	        List<Notification> notifications = notificationDAO.getNotificationsByUserId(user.getUserId());
	
	        // Sort: NEW! first, then by date descending
	        notifications.sort((n1, n2) -> {
	            if (n1.isRead() != n2.isRead()) return Boolean.compare(n1.isRead(), n2.isRead());
	            return n2.getCreatedAt().compareTo(n1.getCreatedAt());
	        });
	
	        System.out.println("\n===== YOUR NOTIFICATIONS =====");
	        if (notifications.isEmpty()) {
	            System.out.println(">>> You have no notifications.");
	            System.out.println("[0] Back");
	        } else {
	            System.out.printf("%-5s | %-10s | %-15s | %s\n", "ID", "STATUS", "DATE", "MESSAGE");
	            System.out.println("-".repeat(80));
	            for (Notification n : notifications) {
	                String status = n.isRead() ? "[READ]" : "[NEW!]";
	                System.out.printf("%-5d | %-10s | %-15s | %s\n", 
	                    n.getNotificationId(), status, n.getCreatedAt().toString().substring(0, 10), n.getMessage());
	            }
	            System.out.println("\nOptions: [1] Mark Read [2] Delete [0] Back");
	            System.out.println("Bulk: Enter IDs separated by commas (e.g. 10,12,15)");
	        }
	
	        System.out.print("Choice: ");
	        String choice = scanner.nextLine().trim();
	
	        if (choice.equals("0")) break;
	
	        if (choice.equals("1") || choice.equals("2")) {
	            if (notifications.isEmpty()) continue;
	            
	            System.out.print("Enter ID(s): ");
	            String input = scanner.nextLine().trim();
	            try {
	                List<Integer> targetIds = Arrays.stream(input.split(","))
	                    .map(String::trim).map(Integer::parseInt).toList();
	
	                if (choice.equals("1")) {
	                    notificationDAO.bulkMarkAsRead(targetIds, user.getUserId());
	                    System.out.println(">>> Marked as read.");
	                } else {
	                    notificationDAO.bulkDelete(targetIds, user.getUserId());
	                    System.out.println(">>> Deleted.");
	                }
	            } catch (Exception e) {
	                System.out.println(">>> Error: Invalid format. Use numbers and commas.");
	            }
	        }
	    }
	}
}
