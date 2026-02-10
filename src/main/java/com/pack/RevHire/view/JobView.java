package com.pack.RevHire.view;

import com.pack.RevHire.model.Job;
import com.pack.RevHire.model.Skill;
import com.pack.RevHire.model.User;
//import com.pack.RevHire.service.JobApplicationService;
import com.pack.RevHire.service.JobService;
import com.pack.RevHire.service.SkillService;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
//import java.util.Map;
import java.util.Scanner;

public class JobView {
    private SkillService skillService = new SkillService();
    private JobService jobService = new JobService();
    private Scanner scanner = new Scanner(System.in);
//    private JobApplicationService appService = new JobApplicationService();

    /**
     * Main entry point for the Job Posting process.
     */
    public void postJobFlow(User employer) {
        System.out.println("\n===== POST A NEW JOB OPENING =====");
        Job job = new Job();
        job.setEmployerId(employer.getUserId());

        try {
            // 1. Collect Basic Job Details
            System.out.print("Job Title: ");
            job.setTitle(scanner.nextLine());

            System.out.print("Job Description: ");
            job.setDescription(scanner.nextLine());

            System.out.print("Years of Experience Required: ");
            job.setExperienceRequired(Integer.parseInt(scanner.nextLine().trim()));

            System.out.print("Education Required (e.g., B.Tech, MCA): ");
            job.setEducationRequired(scanner.nextLine());

            System.out.print("Location: ");
            job.setLocation(scanner.nextLine());

            System.out.print("Minimum Salary: ");
            job.setSalaryMin(Double.parseDouble(scanner.nextLine().trim()));

            System.out.print("Maximum Salary: ");
            job.setSalaryMax(Double.parseDouble(scanner.nextLine().trim()));

            // 2. Select Job Type using Menu
            job.setJobType(selectJobType());

            System.out.print("Application Deadline (YYYY-MM-DD): ");
            job.setDeadline(Date.valueOf(scanner.nextLine().trim()));

            // 3. Collect Skills (Interactive Flow)
            List<Integer> selectedSkills = collectSkillsFlow(null);

            // 4. Submit to Service for Validation and DB Insertion
            int generatedId = jobService.createJob(job, selectedSkills);

            if (generatedId != -1) {
                System.out.println("\n>>> SUCCESS: Job Posted Successfully!");
                System.out.println(">>> Job ID: " + generatedId);
                System.out.println(">>> Status: OPEN");
            } else {
                System.out.println("\n>>> ERROR: Could not post job. Please check validation rules (e.g., Deadline or Salary).");
            }

        } catch (NumberFormatException e) {
            System.out.println(">>> INPUT ERROR: Please enter valid numbers for salary and experience.");
        } catch (IllegalArgumentException e) {
            System.out.println(">>> DATE ERROR: Please use the format YYYY-MM-DD for the deadline.");
        } catch (Exception e) {
            System.out.println(">>> UNEXPECTED ERROR: " + e.getMessage());
        }
    }

    /**
     * Helper to ensure Job Type matches Database Constraints.
     */
    private String selectJobType() {
        while (true) {
            System.out.println("\nSelect Job Type:");
            System.out.println("1. FULL_TIME");
            System.out.println("2. PART_TIME");
            System.out.println("3. INTERNSHIP");
            System.out.println("4. CONTRACT");
            System.out.print("Choice (1-4): ");
            
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": return "FULL_TIME";
                case "2": return "PART_TIME";
                case "3": return "INTERNSHIP";
                case "4": return "CONTRACT";
                default: System.out.println("Invalid selection. Please choose 1, 2, 3, or 4.");
            }
        }
    }

   
     /**
 * Interactive flow to display existing skills and allow custom skill creation.
 * Accepts initialSkills to support editing without losing existing data.
 */
		private List<Integer> collectSkillsFlow(List<Integer> initialSkills) {
		    // Initialize with existing skills if available, otherwise start empty
		    List<Integer> selectedSkillIds = (initialSkills != null) ? new ArrayList<>(initialSkills) : new ArrayList<>();
		    
		    while (true) {
		        System.out.println("\n--- SKILL REQUIREMENTS ---");
		        List<Skill> available = skillService.getAllSkills();
		        
		        // Display skills in a clean grid
		        for (int i = 0; i < available.size(); i++) {
		            Skill s = available.get(i);
		            System.out.printf("[%d] %-15s", s.getSkillId(), s.getSkillName());
		            if ((i + 1) % 4 == 0) System.out.println();
		        }
		
		        System.out.println("\n\nCurrently Selected IDs: " + selectedSkillIds);
		        System.out.println("Options:");
		        System.out.println("1. Enter Skill IDs to ADD (e.g., 1, 5, 10)");
		        System.out.println("2. Add a NEW Skill (not in list)");
		        System.out.println("3. CLEAR all selected skills");
		        System.out.println("0. Finish & Save");
		        System.out.print("Choice: ");
		        
		        String choice = scanner.nextLine().trim();
		
		        if (choice.equals("0")) {
		            if (selectedSkillIds.isEmpty()) {
		                System.out.println(">>> Error: Please select at least one skill.");
		                continue;
		            }
		            break;
		        }
		
		        if (choice.equals("1")) {
		            System.out.print("Enter IDs separated by commas: ");
		            String input = scanner.nextLine();
		            String[] ids = input.split(",");
		            
		            for (String idStr : ids) {
		                try {
		                    int id = Integer.parseInt(idStr.trim());
		                    if (available.stream().anyMatch(s -> s.getSkillId() == id)) {
		                        if (!selectedSkillIds.contains(id)) {
		                            selectedSkillIds.add(id);
		                        }
		                    } else {
		                        System.out.println(">>> Skill ID " + id + " ignored (Not found).");
		                    }
		                } catch (NumberFormatException e) {
		                    System.out.println(">>> Invalid input '" + idStr + "' ignored.");
		                }
		            }
		            System.out.println(">>> IDs processed!");
		
		        } else if (choice.equals("2")) {
		            System.out.print("Enter New Skill Name: ");
		            String newSkill = scanner.nextLine().trim();
		            int newId = skillService.getOrInsertSkill(newSkill);
		            if (newId != -1) {
		                if (!selectedSkillIds.contains(newId)) {
		                    selectedSkillIds.add(newId);
		                }
		                System.out.println(">>> Created and Added!");
		            }
		        } else if (choice.equals("3")) {
		            selectedSkillIds.clear();
		            System.out.println(">>> Selection cleared.");
		        } else {
		            System.out.println(">>> Invalid choice.");
		        }
		    }
		    return selectedSkillIds;
		}
	   
	   public void manageJobPostings(User user) {
		    while (true) {
		        // 1. Fetch only this employer's jobs
		        List<Job> myJobs = jobService.getEmployerJobs(user.getUserId());

		        System.out.println("\n==================== MY JOB POSTINGS ====================");
		        if (myJobs.isEmpty()) {
		            System.out.println(">>> You haven't posted any jobs yet.");
		            return;
		        }

		        // Table Header
		        System.out.printf("%-8s | %-25s | %-12s\n", "JOB ID", "JOB TITLE", "DEADLINE");
		        System.out.println("---------------------------------------------------------");

		        // Table Body (Sorted by deadline DESC in DAO)
		        for (Job j : myJobs) {
		            System.out.printf("%-8d | %-25s | %-12s\n", 
		                j.getJobId(), 
		                j.getTitle().length() > 22 ? j.getTitle().substring(0, 22) + "..." : j.getTitle(),
		                j.getDeadline());
		        }

		        System.out.print("\nEnter Job ID to View Details (or 0 to Go Back): ");
		        int jobId;
		        try {
		            jobId = Integer.parseInt(scanner.nextLine().trim());
		        } catch (NumberFormatException e) {
		            System.out.println(">>> ERROR: Please enter a valid Job ID.");
		            continue;
		        }

		        if (jobId == 0) break;

		        // 2. View full details of the selected job
		        viewJobDetailsFlow(jobId, user.getUserId());
		    }
		} 

		private void viewJobDetailsFlow(int jobId, int employerId) {
		    Job job = jobService.getJobById(jobId);
		    
		    if (job == null || job.getEmployerId() != employerId) {
		        System.out.println(">>> ERROR: Job not found or access denied.");
		        return;
		    }
		
		    List<Skill> skills = jobService.getSkillsByJobId(jobId);
		    // NEW: Get applicant count for Stats
		    int applicantCount = jobService.getApplicantCount(jobId);
		
		    System.out.println("\n------------------ JOB DETAILS ------------------");
		    System.out.println("ID:          " + job.getJobId() + " [" + job.getStatus() + "]"); // Show Status
		    System.out.println("Title:       " + job.getTitle());
		    System.out.println("Applicants:  " + applicantCount); // Show Stats
		    System.out.println("Location:    " + job.getLocation());
		    System.out.println("Salary:      " + job.getSalaryMin() + " - " + job.getSalaryMax());
		    System.out.println("Deadline:    " + job.getDeadline());
		    
		    System.out.print("Required Skills: ");
		    if (skills.isEmpty()) {
		        System.out.println("No skills listed.");
		    } else {
		        System.out.println(skills.stream().map(Skill::getSkillName).collect(java.util.stream.Collectors.joining(", ")));
		    }
		    System.out.println("-------------------------------------------------");
		
		    // UPDATED Action Menu
		    System.out.println("ACTIONS: [1] Edit [2] Close Job [3] Permanent Delete [0] Back");
		    System.out.print("Select Action: ");
		    String action = scanner.nextLine().trim();
		
		    switch (action) {
		        case "1":
		            performEditFlow(job);
		            break;
		        case "2":
		            performCloseJobFlow(jobId);
		            break;
		        case "3":
		            confirmDeleteFlow(jobId);
		            break;
		        case "0":
		            return;
		        default:
		            System.out.println(">>> Invalid action.");
		    }
		}

		private void performCloseJobFlow(int jobId) {
		    System.out.println("\nClosing a job stops new applications but keeps existing data for your records.");
		    System.out.print("Mark Job ID " + jobId + " as CLOSED? (Y/N): ");
		    if (scanner.nextLine().trim().equalsIgnoreCase("Y")) {
		        if (jobService.updateJobStatus(jobId, "CLOSED")) {
		            System.out.println(">>> SUCCESS: Job status updated to CLOSED.");
		        } else {
		            System.out.println(">>> ERROR: Failed to close job.");
		        }
		    }
		}
		
		private void performEditFlow(Job job) {
		    System.out.println("\n--- EDIT JOB MODE ---");
		    System.out.println("Leave blank and press [Enter] to keep current value.");
		
		    // 1. Edit Basic Details (Existing Logic)
		    System.out.print("New Title [" + job.getTitle() + "]: ");
		    String title = scanner.nextLine().trim();
		    if (!title.isEmpty()) job.setTitle(title);
		
		    System.out.print("New Location [" + job.getLocation() + "]: ");
		    String loc = scanner.nextLine().trim();
		    if (!loc.isEmpty()) job.setLocation(loc);
		
		    System.out.print("New Min Salary [" + job.getSalaryMin() + "]: ");
		    String minSal = scanner.nextLine().trim();
		    if (!minSal.isEmpty()) job.setSalaryMin(Double.parseDouble(minSal));
		
		    System.out.print("New Max Salary [" + job.getSalaryMax() + "]: ");
		    String maxSal = scanner.nextLine().trim();
		    if (!maxSal.isEmpty()) job.setSalaryMax(Double.parseDouble(maxSal));
		
		    System.out.print("New Deadline (YYYY-MM-DD) [" + job.getDeadline() + "]: ");
		    String deadlineStr = scanner.nextLine().trim();
		    if (!deadlineStr.isEmpty()) {
		        job.setDeadline(java.sql.Date.valueOf(deadlineStr));
		    }
		
		    // 2. Skill Selection Logic (Updated)
		    System.out.println("\n--- UPDATE SKILLS ---");
		    System.out.print("Do you want to change the required skills for this job? (y/n): ");
		    String changeSkills = scanner.nextLine().trim();
		
		    List<Integer> finalSkills = null;
		    if (changeSkills.equalsIgnoreCase("y")) {
		        // Fetch current skills to pre-populate the selection list
		        List<Skill> currentSkills = jobService.getSkillsByJobId(job.getJobId());
		        List<Integer> existingSkillIds = currentSkills.stream()
		                .map(Skill::getSkillId)
		                .collect(java.util.stream.Collectors.toList());
		
		        // Pass existing IDs so the user can ADD to them instead of starting from []
		        finalSkills = collectSkillsFlow(existingSkillIds);
		    }
		
		    // 3. Save Changes
		    boolean success = jobService.updateJobWithSkills(job, finalSkills);
		    
		    if (success) {
		        System.out.println(">>> SUCCESS: Job and Skills updated successfully!");
		    } else {
		        System.out.println(">>> ERROR: Update failed. Check server logs.");
		    }
		}
		
		private void confirmDeleteFlow(int jobId) {
		    System.out.println("\n!!! WARNING: Deleting a job is permanent. This will also remove applicant links.");
		    System.out.print("Are you sure you want to delete Job ID " + jobId + "? (Y/N): ");
		    
		    String confirm = scanner.nextLine().trim().toUpperCase();
		    
		    if (confirm.equals("Y")) {
		        boolean deleted = jobService.removeJob(jobId);
		        if (deleted) {
		            System.out.println(">>> SUCCESS: Job has been removed.");
		        } else {
		            System.out.println(">>> ERROR: Could not delete the job.");
		        }
		    } else {
		        System.out.println(">>> Deletion cancelled.");
		    }
		}
		
		
//		
}