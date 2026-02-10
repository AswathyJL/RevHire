package com.pack.RevHire.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import com.pack.RevHire.model.Certification;
import com.pack.RevHire.model.Education;
import com.pack.RevHire.model.Experience;
import com.pack.RevHire.model.JobSeekerProfile;
import com.pack.RevHire.model.Project;
import com.pack.RevHire.model.Skill;
import com.pack.RevHire.model.User;
import com.pack.RevHire.service.ResumeService;
import com.pack.RevHire.service.SkillService;
import com.pack.RevHire.service.UserService;

public class ResumeView {
    private Scanner scanner = new Scanner(System.in);
    private ResumeService resumeService = new ResumeService();
    private SkillService skillService = new SkillService();
    private UserService userService = new UserService();

    public void buildResumeMenu(User user) {
        // Step 1: Ensure the user has a resume record in the DB before proceeding
        int resumeId = resumeService.getOrCreateResumeId(user.getUserId());

        while (true) {
            System.out.println("\n===== BUILD/EDIT RESUME =====");
            System.out.println("1. Professional Objective");
            System.out.println("2. Skills");
            System.out.println("3. Education");
            System.out.println("4. Experience");
            System.out.println("5. Projects");
            System.out.println("6. Certifications");
            System.out.println("7. View Full Resume");
            System.out.println("0. Back to Dashboard");
            System.out.print("Select section: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println(">>> Invalid input. Please enter a number.");
                continue;
            }

            if (choice == 0) break;

//            switch (choice) {
//                case 1: manageObjectiveFlow(resumeId); break;
//                case 2: manageSkillsFlow(resumeId); break;
//                case 3: manageEducationFlow(resumeId); break;
//                case 4: manageExperienceFlow(resumeId); break;
//                case 5: manageProjectsFlow(resumeId); break;
//                case 6: manageCertificationsFlow(resumeId); break;
//                case 7: viewFullResumeFlow(resumeId); break;
//                default: System.out.println(">>> Invalid choice.");
//            }
            switch (choice) {
            case 1:  manageObjectiveFlow(resumeId); break;
            case 2:  manageSkillsFlow(resumeId);  break;
            case 3:  manageEducationFlow(resumeId); break;
            case 4:  manageExperienceFlow(resumeId); break;
            case 5:  manageProjectFlow(resumeId); break;
            case 6:  manageCertificationFlow(resumeId);break;
            case 7: 
            	JobSeekerProfile profile = userService.getJobSeekerProfile(user.getUserId());
                
                // Get the email directly from the user object passed to buildResumeMenu
                String userEmail = user.getEmail(); 

                if (profile != null) {
                    viewFullResume(resumeId, profile, userEmail);
                } else {
                    System.out.println("\n>>> [!] Profile Incomplete.");
                    System.out.println(">>> Please go to 'Manage Profile' to add your name, phone, and location first.");
                }
            	break;
            default: System.out.println(">>> Invalid choice.");
        }
        }
    }
    
//  *********************************************************
//  OBJECTIVE
//	*********************************************************    

    private void manageObjectiveFlow(int resumeId) {
        // Step 1: Display Current
        String currentObjective = resumeService.getObjective(resumeId);
        System.out.println("\n--- Current Objective ---");
        System.out.println(currentObjective != null ? currentObjective : "[No objective set]");
        
        // Step 2: Confirm
        System.out.print("\nWould you like to edit? (yes/no): ");
        if (scanner.nextLine().equalsIgnoreCase("yes")) {
            System.out.println("Enter your professional objective:");
            String newObjective = scanner.nextLine().trim();
            
            // Step 3: Save
            if (resumeService.updateObjective(resumeId, newObjective)) {
                System.out.println(">>> Objective updated!");
            }
        }
    }
    
//  *********************************************************
//  SKILLS
//	*********************************************************
    
    private void manageSkillsFlow(int resumeId) {
        while (true) {
            // 1. Display Current Skills already linked to this Resume
            List<Skill> mySkills = resumeService.getSkillsByResumeId(resumeId);
            System.out.println("\n--- YOUR CURRENT SKILLS ---");
            if (mySkills.isEmpty()) {
                System.out.println("[No skills added yet]");
            } else {
                System.out.println(mySkills.stream()
                    .map(Skill::getSkillName)
                    .collect(java.util.stream.Collectors.joining(" | ")));
            }

            // 2. Display Master List from SkillService
            System.out.println("\n--- AVAILABLE MASTER LIST ---");
            List<Skill> masterList = skillService.getAllSkills();
            for (int i = 0; i < masterList.size(); i++) {
                Skill s = masterList.get(i);
                System.out.printf("[%d] %-15s", s.getSkillId(), s.getSkillName());
                if ((i + 1) % 4 == 0) System.out.println(); // 4 skills per line for grid view
            }

            System.out.println("\n\nACTIONS:");
            System.out.println("1. Select Skills by ID (e.g., 1, 4, 12)");
            System.out.println("2. Add a Custom Skill (Not in list)");
            System.out.println("3. Remove a Skill");
            System.out.println("0. Back to Resume Menu");
            System.out.print("Choice: ");

            String choice = scanner.nextLine().trim();
            if (choice.equals("0")) break;

            switch (choice) {
                case "1":
                    processSkillIds(resumeId);
                    break;
                case "2":
                    processCustomSkill(resumeId);
                    break;
                case "3":
                    processRemoveSkill(resumeId);
                    break;
                default:
                    System.out.println(">>> Invalid choice.");
            }
        }
    }

   private void processSkillIds(int resumeId)
   {
	    System.out.print("Enter IDs separated by commas: ");
	    String input = scanner.nextLine().trim();
	    if (input.isEmpty()) return;
	
	    // 1. Get the latest Master List to validate against
	    List<Skill> available = skillService.getAllSkills();
	    // Convert to a Set of IDs for lightning-fast lookup
	    Set<Integer> validIds = available.stream()
	                                     .map(Skill::getSkillId)
	                                     .collect(Collectors.toSet());
	
	    String[] inputIds = input.split(",");
	    List<Integer> invalidInputs = new ArrayList<>();
	    int successCount = 0;
	
	    for (String idStr : inputIds) {
	        try {
	            int id = Integer.parseInt(idStr.trim());
	            
	            // 2. CHECK: Does this ID exist in our Master List?
	            if (validIds.contains(id)) {
	                resumeService.addSkillToResume(resumeId, id);
	                successCount++;
	            } else {
	                invalidInputs.add(id); // Flag for later
	            }
	        } catch (NumberFormatException e) {
	            System.out.println(">>> Skipping non-numeric input: " + idStr);
	        }
	    }
	
	    // 3. User Feedback
	    if (successCount > 0) {
	        System.out.println(">>> SUCCESS: " + successCount + " skill(s) added.");
	    }
	    
	    if (!invalidInputs.isEmpty()) {
	        System.out.println(">>> WARNING: The following IDs do not exist and were skipped: " + invalidInputs);
	        System.out.println(">>> Tip: Use Option 2 to add a brand new skill name.");
	    }
	}

    private void processCustomSkill(int resumeId) {
        System.out.print("Enter New Skill Name: ");
        String newSkill = scanner.nextLine().trim();
        
        if (!newSkill.isEmpty()) {
            // First, ensure it exists in Master List (SkillService handles this)
            int skillId = skillService.getOrInsertSkill(newSkill);
            
            if (skillId != -1) {
                // Then, link it to the Resume
                resumeService.addSkillToResume(resumeId, skillId);
                System.out.println(">>> '" + newSkill + "' added to your resume!");
            }
        }
    }

    private void processRemoveSkill(int resumeId) {
        // 1. Fetch skills specifically for this resume
        List<Skill> currentSkills = resumeService.getSkillsByResumeId(resumeId);
        
        if (currentSkills.isEmpty()) {
            System.out.println(">>> You have no skills to remove.");
            return;
        }

        System.out.println("\n--- REMOVE A SKILL ---");
        for (Skill s : currentSkills) {
            System.out.printf("[%d] %s\n", s.getSkillId(), s.getSkillName());
        }
        
        System.out.print("Enter the ID of the skill to remove (or 0 to cancel): ");
        try {
            int idToRemove = Integer.parseInt(scanner.nextLine().trim());
            
            if (idToRemove == 0) return;

            // 2. Call Service to handle the deletion
            boolean success = resumeService.removeSkillFromResume(resumeId, idToRemove);
            
            if (success) {
                System.out.println(">>> Skill removed from your resume successfully.");
            } else {
                System.out.println(">>> Error: Skill ID not found on your resume.");
            }
        } catch (NumberFormatException e) {
            System.out.println(">>> Invalid input. Please enter a numeric ID.");
        }
    }
    
//  *********************************************************
//  EDUCATION
//	*********************************************************
    
    private void manageEducationFlow(int resumeId) {
        while (true) {
            // 1. Fetch and Display existing Education
            List<Education> eduList = resumeService.getEducationByResumeId(resumeId);
            
            System.out.println("\n--- YOUR EDUCATION HISTORY ---");
            if (eduList.isEmpty()) {
                System.out.println("[No education details added yet]");
            } else {
                // Table Header
                System.out.printf("%-5s | %-20s | %-25s | %-12s\n", "ID", "DEGREE", "INSTITUTION", "YEARS");
                System.out.println("-------------------------------------------------------------------------");
                
                for (Education e : eduList) {
                    // 1. Prepare the display string for the end year
                    String endYearDisplay = (e.getEndYear() == 0) ? "Present" : String.valueOf(e.getEndYear());

                    // 2. Change the last %d to %s to handle both numbers and the word "Present"
                    System.out.printf("%-5d | %-20s | %-25s | %d - %-12s\n", 
                        e.getEducationId(), 
                        e.getDegree(), 
                        e.getInstitution(), 
                        e.getStartYear(), 
                        endYearDisplay); 
                }
            }

            System.out.println("\nACTIONS: [1] Add [2] Edit [3] Delete [0] Back");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("0")) break;

            switch (choice) {
                case "1":
                    addEducationFlow(resumeId);
                    break;
                case "2":
                    editEducationFlow(resumeId);
                    break;
                case "3":
                    deleteEducationFlow(resumeId);
                    break;
                default:
                    System.out.println(">>> Invalid choice.");
            }
        }
    }

    private void addEducationFlow(int resumeId) {
        Education edu = new Education();
        edu.setResumeId(resumeId);

        try {
            System.out.println("\n--- ADD NEW EDUCATION ---");
            System.out.print("Degree (e.g., B.Tech): ");
            edu.setDegree(scanner.nextLine().trim());

            System.out.print("Institution: ");
            edu.setInstitution(scanner.nextLine().trim());

            System.out.print("Start Year (YYYY): ");
            edu.setStartYear(Integer.parseInt(scanner.nextLine().trim()));

            System.out.print("End Year (YYYY or enter 0 if currently studying): ");
            edu.setEndYear(Integer.parseInt(scanner.nextLine().trim()));

            // Simple validation
            if (edu.getEndYear() != 0 && edu.getEndYear() < edu.getStartYear()) {
                System.out.println(">>> ERROR: End year cannot be before start year.");
                return;
            }

            boolean success = resumeService.addEducation(edu);
            if (success) {
                System.out.println(">>> Education added successfully!");
            } else {
                System.out.println(">>> ERROR: Failed to save education.");
            }
        } catch (NumberFormatException e) {
            System.out.println(">>> INPUT ERROR: Please enter valid numbers for years.");
        }
    }
    
    private void editEducationFlow(int resumeId) {
        System.out.print("\nEnter Education ID to edit: ");
        try {
            int eduId = Integer.parseInt(scanner.nextLine().trim());
            
            // 1. Fetch current details to show the user what they are changing
            Education existing = resumeService.getEducationById(eduId);
            if (existing == null || existing.getResumeId() != resumeId) {
                System.out.println(">>> ERROR: Education record not found.");
                return;
            }

            System.out.println("--- EDITING MODE (Leave blank to keep current value) ---");

            System.out.print("Degree [" + existing.getDegree() + "]: ");
            String degree = scanner.nextLine().trim();
            if (!degree.isEmpty()) existing.setDegree(degree);

            System.out.print("Institution [" + existing.getInstitution() + "]: ");
            String inst = scanner.nextLine().trim();
            if (!inst.isEmpty()) existing.setInstitution(inst);

            System.out.print("Start Year [" + existing.getStartYear() + "]: ");
            String sYear = scanner.nextLine().trim();
            if (!sYear.isEmpty()) existing.setStartYear(Integer.parseInt(sYear));

            System.out.print("End Year [" + existing.getEndYear() + "]: ");
            String eYear = scanner.nextLine().trim();
            if (!eYear.isEmpty()) existing.setEndYear(Integer.parseInt(eYear));

            // 2. Save changes
            boolean success = resumeService.updateEducation(existing);
            if (success) {
                System.out.println(">>> SUCCESS: Education updated!");
            } else {
                System.out.println(">>> ERROR: Update failed.");
            }
        } catch (NumberFormatException e) {
            System.out.println(">>> INPUT ERROR: Please enter valid numbers.");
        }
    }
    
    private void deleteEducationFlow(int resumeId) {
        System.out.print("\nEnter Education ID to delete (check the ID in the table above): ");
        try {
            int eduId = Integer.parseInt(scanner.nextLine().trim());
            boolean success = resumeService.deleteEducation(eduId);
            if (success) {
                System.out.println(">>> Education entry removed.");
            } else {
                System.out.println(">>> ERROR: ID not found or could not be deleted.");
            }
        } catch (NumberFormatException e) {
            System.out.println(">>> Invalid input.");
        }
    }
    
//  *********************************************************
//  EXPERIENCE
//	*********************************************************
    
    private void manageExperienceFlow(int resumeId) {
        while (true) {
            // 1. Fetch experiences sorted by DAO (Present first, then descending end_date)
            List<Experience> expList = resumeService.getExperienceByResumeId(resumeId);
            
            System.out.println("\n--- WORK EXPERIENCE ---");
            if (expList.isEmpty()) {
                System.out.println("[No experience added yet]");
            } else {
                System.out.printf("%-5s | %-20s | %-20s | %-25s\n", "ID", "ROLE", "COMPANY", "DURATION");
                System.out.println("-----------------------------------------------------------------------------------");
                for (Experience e : expList) {
                    String endDateStr = (e.getEndDate() == null) ? "Present" : e.getEndDate().toString();
                    System.out.printf("%-5d | %-20s | %-20s | %s to %s\n", 
                        e.getExperienceId(), e.getJobRole(), e.getCompany(), e.getStartDate(), endDateStr);
                }
            }

            System.out.println("\nACTIONS: [1] Add [2] Edit [3] Delete [0] Back");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("0")) break;

            switch (choice) {
                case "1": addExperienceFlow(resumeId); break;
                case "2": editExperienceFlow(resumeId); break;
                case "3": deleteExperienceFlow(resumeId); break;
                default: System.out.println(">>> Invalid choice.");
            }
        }
    }

    private void addExperienceFlow(int resumeId) {
        try {
            Experience exp = new Experience();
            exp.setResumeId(resumeId);

            System.out.print("Company Name: ");
            exp.setCompany(scanner.nextLine().trim());

            System.out.print("Job Role (e.g. Java Developer): ");
            exp.setJobRole(scanner.nextLine().trim());

            System.out.print("Start Date (yyyy-mm-dd): ");
            exp.setStartDate(java.sql.Date.valueOf(scanner.nextLine().trim()));

            System.out.print("End Date (yyyy-mm-dd or leave blank if Currently Working): ");
            String endInput = scanner.nextLine().trim();
            if (endInput.isEmpty()) {
                exp.setEndDate(null);
            } else {
                exp.setEndDate(java.sql.Date.valueOf(endInput));
            }

            System.out.print("Description/Responsibilities: ");
            exp.setDescription(scanner.nextLine().trim());

            // Validation
            if (exp.getEndDate() != null && exp.getEndDate().before(exp.getStartDate())) {
                System.out.println(">>> ERROR: End date cannot be before start date.");
                return;
            }

            if (resumeService.addExperience(exp)) {
                System.out.println(">>> Experience added successfully!");
            }
        } catch (IllegalArgumentException e) {
            System.out.println(">>> INPUT ERROR: Please use the format yyyy-mm-dd.");
        }
    }

    private void editExperienceFlow(int resumeId) {
        System.out.print("\nEnter Experience ID to edit: ");
        try {
            int expId = Integer.parseInt(scanner.nextLine().trim());
            Experience existing = resumeService.getExperienceById(expId);

            if (existing == null || existing.getResumeId() != resumeId) {
                System.out.println(">>> ERROR: Record not found.");
                return;
            }

            System.out.println("--- EDITING (Leave blank to keep current) ---");

            System.out.print("Company [" + existing.getCompany() + "]: ");
            String comp = scanner.nextLine().trim();
            if (!comp.isEmpty()) existing.setCompany(comp);

            System.out.print("Role [" + existing.getJobRole() + "]: ");
            String role = scanner.nextLine().trim();
            if (!role.isEmpty()) existing.setJobRole(role);

            System.out.print("Start Date [" + existing.getStartDate() + "]: ");
            String sDate = scanner.nextLine().trim();
            if (!sDate.isEmpty()) existing.setStartDate(java.sql.Date.valueOf(sDate));

            System.out.print("End Date [" + (existing.getEndDate() == null ? "Present" : existing.getEndDate()) + "] (Type 'present' to set current): ");
            String eDate = scanner.nextLine().trim();
            if (!eDate.isEmpty()) {
                if (eDate.equalsIgnoreCase("present")) {
                    existing.setEndDate(null);
                } else {
                    existing.setEndDate(java.sql.Date.valueOf(eDate));
                }
            }
            
            System.out.print("Description [" + existing.getDescription() + "]: ");
            String desc = scanner.nextLine().trim();
            if (!desc.isEmpty()) {
                existing.setDescription(desc);
            }

            // Re-validate dates after edit
            if (existing.getEndDate() != null && existing.getEndDate().before(existing.getStartDate())) {
                System.out.println(">>> ERROR: End date cannot be before start date. Update aborted.");
                return;
            }

            if (resumeService.updateExperience(existing)) {
                System.out.println(">>> Experience updated successfully!");
            }
        } catch (Exception e) {
            System.out.println(">>> ERROR: Invalid input format.");
        }
    }

    private void deleteExperienceFlow(int resumeId) {
        System.out.print("\nEnter Experience ID to delete: ");
        try {
            int expId = Integer.parseInt(scanner.nextLine().trim());
            if (resumeService.deleteExperience(expId)) {
                System.out.println(">>> Experience deleted.");
            } else {
                System.out.println(">>> ERROR: Could not delete.");
            }
        } catch (NumberFormatException e) {
            System.out.println(">>> Invalid ID.");
        }
    }
    
//  *********************************************************
//  PROJECTS
//	*********************************************************
    
	  private void manageProjectFlow(int resumeId) {
	    while (true) {
	        List<Project> projectList = resumeService.getProjectsByResumeId(resumeId);
	        
	        System.out.println("\n======================== YOUR PROJECTS ========================");
	        if (projectList.isEmpty()) {
	            System.out.println("[No projects added yet]");
	        } else {
	            for (Project p : projectList) {
	                System.out.println("ID: " + p.getProjectId() + " | TITLE: " + p.getTitle());
	                
	                // Description wrapping (Neat display)
	                System.out.println("Description: " + formatLongText(p.getDescription(), 70));
	                
	                // Tech Stack Display
	                System.out.println("Tech Stack: [" + p.getTechStack() + "]");
	                System.out.println("---------------------------------------------------------------");
	            }
	        }
	
	        System.out.println("ACTIONS: [1] Add [2] Edit [3] Delete [0] Back");
	        System.out.print("Choice: ");
	        String choice = scanner.nextLine().trim();
	
	        if (choice.equals("0")) break;
	        switch (choice) {
	            case "1": addProjectFlow(resumeId); break;
	            case "2": editProjectFlow(resumeId); break;
	            case "3": deleteProjectFlow(resumeId); break;
	            default: System.out.println(">>> Invalid choice.");
	        }
	    }
	}

    private void addProjectFlow(int resumeId) {
	    Project p = new Project();
	    p.setResumeId(resumeId);
	
	    System.out.print("Project Title: ");
	    p.setTitle(scanner.nextLine().trim());
	    System.out.print("Description: ");
	    p.setDescription(scanner.nextLine().trim());
	
	    // --- Dynamic Tech Stack Building ---
	    String techStack = buildTechStack("");
	    p.setTechStack(techStack);
	
	    if (resumeService.addProject(p)) {
	        System.out.println(">>> Project added successfully with technologies: " + techStack);
	    }
	}

	private String buildTechStack(String existingStack) {
	    List<String> selectedSkills = new ArrayList<>();
	    
	 // If there is an existing stack, prepopulate the list
	    if (existingStack != null && !existingStack.isEmpty()) {
	        for (String skill : existingStack.split(",")) {
	            selectedSkills.add(skill.trim());
	        }
	    }
	    
	    while (true) {
	        System.out.println("\n--- BUILD TECH STACK ---");
	        System.out.println("Current Stack: " + (selectedSkills.isEmpty() ? "[Empty]" : String.join(", ", selectedSkills)));
	        System.out.println("1. Add from existing Skills");
	        System.out.println("2. Add a new custom Skill");
	        System.out.println("0. Finish Tech Stack");
	        System.out.print("Choice: ");
	        
	        String choice = scanner.nextLine().trim();
	        if (choice.equals("0")) break;
	
	        if (choice.equals("1")) {
	        	displayAvailableSkillsGrid();
	            // Reusing your existing skill list logic
	            List<Skill> master = skillService.getAllSkills();
	            for (Skill s : master) {
	                System.out.printf("[%d] %s ", s.getSkillId(), s.getSkillName());
	            }
	            System.out.print("\nEnter Skill IDs (comma separated): ");
	            String[] ids = scanner.nextLine().split(",");
	            
	            // Map IDs back to Names for our String
	            for (String idStr : ids) {
	                try {
	                    int id = Integer.parseInt(idStr.trim());
	                    master.stream()
	                          .filter(s -> s.getSkillId() == id)
	                          .findFirst()
	                          .ifPresent(s -> selectedSkills.add(s.getSkillName()));
	                } catch (Exception e) { /* Skip invalid IDs */ }
	            }
	        } else if (choice.equals("2")) {
	            System.out.print("Enter custom skill name: ");
	            selectedSkills.add(scanner.nextLine().trim());
	        }
	    }
	    // Convert List to "Java, Spring, SQL"
	    return String.join(", ", selectedSkills);
	}
	
	private void displayAvailableSkillsGrid() {
	    List<Skill> skills = skillService.getAllSkills();
	    System.out.println("\n--- AVAILABLE SKILLS ---");
	    int count = 0;
	    for (Skill s : skills) {
	        System.out.printf("[%d] %-15s ", s.getSkillId(), s.getSkillName());
	        count++;
	        if (count % 4 == 0) System.out.println(); // Every 4 skills, start a new line
	    }
	    System.out.println();
	}
	
	private String formatLongText(String text, int limit) {
	    if (text == null || text.length() <= limit) return text;
	    
	    StringBuilder sb = new StringBuilder(text);
	    int i = 0;
	    while ((i = sb.lastIndexOf(" ", i + limit)) != -1) {
	        // Replace the space closest to the limit with a newline + indentation
	        sb.replace(i, i + 1, "\n             "); 
	        if (i + limit + 14 >= sb.length()) break; 
	    }
	    return sb.toString();
	}

    private void editProjectFlow(int resumeId) {
	    System.out.print("\nEnter Project ID to edit: ");
	    try {
	        int pid = Integer.parseInt(scanner.nextLine().trim());
	        Project existing = resumeService.getProjectById(pid);
	
	        if (existing == null || existing.getResumeId() != resumeId) {
	            System.out.println(">>> ERROR: Project not found.");
	            return;
	        }
	
	        System.out.println("--- EDITING (Leave blank to keep current) ---");
	        
	        System.out.print("Title [" + existing.getTitle() + "]: ");
	        String title = scanner.nextLine().trim();
	        if (!title.isEmpty()) existing.setTitle(title);
	
	        System.out.print("New Description (Press Enter to keep existing): ");
	        String desc = scanner.nextLine().trim();
	        if (!desc.isEmpty()) existing.setDescription(desc);
	
	        // --- THE SKILL EDITING PART ---
	        System.out.println("Current Tech Stack: " + existing.getTechStack());
	        System.out.print("Do you want to update the Tech Stack? (y/n): ");
	       if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
			   
			    String newStack = buildTechStack(existing.getTechStack()); 
			    existing.setTechStack(newStack);
			}
	
	        if (resumeService.updateProject(existing)) {
	            System.out.println(">>> Project updated successfully!");
	        }
	    } catch (Exception e) {
	        System.out.println(">>> ERROR: Invalid input.");
	    }
	}
    
    private void deleteProjectFlow(int resumeId) {
        System.out.print("\nEnter Project ID to delete (refer to the table above): ");
        try {
            int projectId = Integer.parseInt(scanner.nextLine().trim());
            
            // Confirmation is always good practice for deletions
            System.out.print("Are you sure you want to delete project ID " + projectId + "? (y/n): ");
            String confirm = scanner.nextLine().trim();
            
            if (confirm.equalsIgnoreCase("y")) {
                // Call service to perform the delete
                boolean success = resumeService.deleteProject(projectId);
                
                if (success) {
                    System.out.println(">>> SUCCESS: Project has been removed from your resume.");
                } else {
                    System.out.println(">>> ERROR: Project ID not found or could not be deleted.");
                }
            } else {
                System.out.println(">>> Deletion cancelled.");
            }
        } catch (NumberFormatException e) {
            System.out.println(">>> INVALID INPUT: Please enter a numeric Project ID.");
        }
    }
    
    
//  *********************************************************
//  CERTIFICATIONS
//	*********************************************************
    
    
    private void manageCertificationFlow(int resumeId) {
        while (true) {
            List<Certification> certList = resumeService.getCertificationsByResumeId(resumeId);
            
            System.out.println("\n--- CERTIFICATIONS ---");
            if (certList.isEmpty()) {
                System.out.println("[No certifications added yet]");
            } else {
                System.out.printf("%-5s | %-25s | %-20s | %-12s\n", "ID", "CERTIFICATE NAME", "ORGANIZATION", "ISSUE DATE");
                System.out.println("-------------------------------------------------------------------------");
                for (Certification c : certList) {
                    System.out.printf("%-5d | %-25s | %-20s | %s\n", 
                        c.getCertId(), c.getCertName(), c.getIssuingOrganization(), c.getIssueDate());
                }
            }

            System.out.println("\nACTIONS: [1] Add [2] Edit [3] Delete [0] Back");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();
            if (choice.equals("0")) break;

            switch (choice) {
                case "1": addCertificationFlow(resumeId); break;
                case "2": editCertificationFlow(resumeId); break;
                case "3": deleteCertificationFlow(resumeId); break;
                default: System.out.println(">>> Invalid choice.");
            }
        }
    }

    private void addCertificationFlow(int resumeId) {
        try {
            Certification cert = new Certification();
            cert.setResumeId(resumeId);

            System.out.print("Certificate Name: ");
            cert.setCertName(scanner.nextLine().trim());
            System.out.print("Issuing Organization: ");
            cert.setIssuingOrganization(scanner.nextLine().trim());
            System.out.print("Issue Date (yyyy-mm-dd): ");
            cert.setIssueDate(java.sql.Date.valueOf(scanner.nextLine().trim()));

            if (resumeService.addCertification(cert)) {
                System.out.println(">>> Certification added successfully!");
            }
        } catch (IllegalArgumentException e) {
            System.out.println(">>> INPUT ERROR: Use yyyy-mm-dd format.");
        }
    }

    private void editCertificationFlow(int resumeId) {
        System.out.print("\nEnter Certification ID to edit: ");
        try {
            int cid = Integer.parseInt(scanner.nextLine().trim());
            Certification existing = resumeService.getCertificationById(cid);

            if (existing == null || existing.getResumeId() != resumeId) {
                System.out.println(">>> ERROR: Certification not found.");
                return;
            }

            System.out.println("--- EDITING (Leave blank to keep current) ---");
            System.out.print("Name [" + existing.getCertName() + "]: ");
            String name = scanner.nextLine().trim();
            if (!name.isEmpty()) existing.setCertName(name);

            System.out.print("Organization [" + existing.getIssuingOrganization() + "]: ");
            String org = scanner.nextLine().trim();
            if (!org.isEmpty()) existing.setIssuingOrganization(org);

            System.out.print("Date [" + existing.getIssueDate() + "] (yyyy-mm-dd): ");
            String dateStr = scanner.nextLine().trim();
            if (!dateStr.isEmpty()) existing.setIssueDate(java.sql.Date.valueOf(dateStr));

            if (resumeService.updateCertification(existing)) {
                System.out.println(">>> Certification updated!");
            }
        } catch (Exception e) {
            System.out.println(">>> ERROR: Invalid input.");
        }
    }
    
    private void deleteCertificationFlow(int resumeId) {
        System.out.print("\nEnter Certification ID to delete: ");
        try {
            int certId = Integer.parseInt(scanner.nextLine().trim());
            
            // Safety check: Verify the certificate belongs to this resume
            Certification existing = resumeService.getCertificationById(certId);
            
            if (existing == null || existing.getResumeId() != resumeId) {
                System.out.println(">>> ERROR: Certification record not found.");
                return;
            }

            System.out.print("Are you sure you want to delete '" + existing.getCertName() + "'? (y/n): ");
            String confirm = scanner.nextLine().trim();
            
            if (confirm.equalsIgnoreCase("y")) {
                if (resumeService.deleteCertification(certId)) {
                    System.out.println(">>> SUCCESS: Certification deleted.");
                } else {
                    System.out.println(">>> ERROR: Could not delete record.");
                }
            } else {
                System.out.println(">>> Deletion cancelled.");
            }
        } catch (NumberFormatException e) {
            System.out.println(">>> INVALID INPUT: Please enter a numeric ID.");
        }
    }
    
    
//  *********************************************************
//  VIEW FULL RESUME
//	*********************************************************
    
	public void viewFullResume(int resumeId, JobSeekerProfile profile, String email) {
	    while (true) {
	        // Clear screen feel with a strong border
	        System.out.println("\n\n" + "=".repeat(80));
	        
	        // --- 1. HEADER SECTION ---
	        // Uses JobSeekerProfile for Name, Phone, and Location
	        System.out.println(centerText(profile.getFullName().toUpperCase(), 80));
	        String contactInfo = String.format("%s  |  %s  |  %s", profile.getPhone(), email, profile.getLocation());
	        System.out.println(centerText(contactInfo, 80));
	        System.out.println("-".repeat(80));
	
	        // --- 2. OBJECTIVE SECTION ---
	        System.out.println(centerText("PROFESSIONAL SUMMARY", 80));
	        String objective = resumeService.getObjective(resumeId);
	        System.out.println(objective != null && !objective.isEmpty() ? objective : "No summary provided.");
	        System.out.println("-".repeat(80));
	
	        // --- 3. SKILLS SECTION ---
	        System.out.println(centerText("TECHNICAL SKILLS", 80));
	        List<Skill> skills = resumeService.getSkillsByResumeId(resumeId);
	        String skillLine = skills.stream()
	                                 .map(Skill::getSkillName)
	                                 .collect(Collectors.joining(" | "));
	        System.out.println(centerText(skillLine.isEmpty() ? "No skills added." : skillLine, 80));
	        System.out.println("-".repeat(80));
	
	        // --- 4. EDUCATION SECTION ---
	        System.out.println(centerText("EDUCATION", 80));
	        List<Education> eduList = resumeService.getEducationByResumeId(resumeId);
	        if (eduList == null || eduList.isEmpty()) {
	            System.out.println(centerText("[No education records]", 80));
	        } else {
	            for (Education edu : eduList) {
	                // Utilizing your getFormattedEndYear() method
	                String years = edu.getStartYear() + " - " + edu.getFormattedEndYear();
	                System.out.printf("** %-55s %20s\n", edu.getDegree(), years);
	                System.out.println("   " + edu.getInstitution());
	            }
	        }
	        System.out.println("-".repeat(80));
	
	        // --- 5. EXPERIENCE SECTION ---
	        // Showing Total Experience from profile
	        System.out.println(centerText("WORK EXPERIENCE (" + profile.getTotalExperience() + " Years Total)", 80));
	        List<Experience> expList = resumeService.getExperienceByResumeId(resumeId);
	        if (expList == null || expList.isEmpty()) {
	            System.out.println(centerText("[No experience records]", 80));
	        } else {
	            for (Experience e : expList) {
	                String end = (e.getEndDate() == null) ? "Present" : e.getEndDate().toString();
	                System.out.printf("** %-55s %20s\n", e.getJobRole() + " at " + e.getCompany(), e.getStartDate() + " to " + end);
	                System.out.println("   " + e.getDescription());
	            }
	        }
	        System.out.println("-".repeat(80));
	
	        // --- 6. PROJECTS SECTION ---
	        System.out.println(centerText("KEY PROJECTS", 80));
	        List<Project> projList = resumeService.getProjectsByResumeId(resumeId);
	        if (projList == null || projList.isEmpty()) {
	            System.out.println(centerText("[No projects added]", 80));
	        } else {
	            for (Project p : projList) {
	                System.out.println("** " + p.getTitle());
	                System.out.println("   Tech Stack: " + p.getTechStack());
	                System.out.println("   " + p.getDescription());
	            }
	        }
	        System.out.println("-".repeat(80));
	
	        // --- 7. CERTIFICATIONS SECTION ---
	        System.out.println(centerText("CERTIFICATIONS", 80));
	        List<Certification> certList = resumeService.getCertificationsByResumeId(resumeId);
	        if (certList == null || certList.isEmpty()) {
	            System.out.println(centerText("[No certifications added]", 80));
	        } else {
	            for (Certification c : certList) {
	                System.out.printf("** %-55s %20s\n", c.getCertName() + " (" + c.getIssuingOrganization() + ")", c.getIssueDate());
	            }
	        }
	        System.out.println("=".repeat(80));
	
	        // --- NAVIGATION DASHBOARD ---
	        System.out.println("\n" + centerText("RESUME MANAGEMENT PANEL", 80));
	        System.out.println("[1] Edit Objective  [2] Manage Skills     [3] Manage Education");
	        System.out.println("[4] Manage Experience [5] Manage Projects  [6] Manage Certs");
	        System.out.println("[0] Back to Main Menu");
	        System.out.print("\nChoice: ");
	        
	        String choice = scanner.nextLine().trim();
	        if (choice.equals("0")) break;
	
	        switch (choice) {
	            case "1": editObjectiveFlow(resumeId); break;
	            case "2": manageSkillsFlow(resumeId); break;
	            case "3": manageEducationFlow(resumeId); break;
	            case "4": manageExperienceFlow(resumeId); break;
	            case "5": manageProjectFlow(resumeId); break;
	            case "6": manageCertificationFlow(resumeId); break;
	            default: System.out.println(">>> Invalid Selection.");
	        }
	    }
	}
	
	// Helper: Centers text within the provided width
	private String centerText(String text, int width) {
	    if (text == null || text.length() >= width) return text;
	    int padding = (width - text.length()) / 2;
	    return " ".repeat(padding) + text;
	}
	
	// Helper: Handles the Objective Update
	private void editObjectiveFlow(int resumeId) {
	    System.out.println("\n--- UPDATE PROFESSIONAL SUMMARY ---");
	    String current = resumeService.getObjective(resumeId);
	    System.out.println("Current: " + (current != null ? current : "None"));
	    
	    System.out.print("Enter New Objective (or press Enter to keep current): ");
	    String newObj = scanner.nextLine().trim();
	    
	    if (!newObj.isEmpty()) {
	        if (resumeService.updateObjective(resumeId, newObj)) {
	            System.out.println(">>> Summary updated successfully!");
	        } else {
	            System.out.println(">>> Error updating summary.");
	        }
	    }
	}
	
	// *********************************************************
    // 1. FOR JOB SEEKER: View + Edit Menu
    // *********************************************************
    public void manageResumeFlow(int resumeId, JobSeekerProfile profile, String email) {
        while (true) {
            // Call the shared display method
            displayResumeStatic(resumeId, profile, email);

            // --- NAVIGATION DASHBOARD (Seeker Only) ---
            System.out.println("\n" + centerText("RESUME MANAGEMENT PANEL", 80));
            System.out.println("[1] Edit Objective  [2] Manage Skills      [3] Manage Education");
            System.out.println("[4] Manage Experience [5] Manage Projects  [6] Manage Certs");
            System.out.println("[0] Back to Main Menu");
            System.out.print("\nChoice: ");

            String choice = scanner.nextLine().trim();
            if (choice.equals("0")) break;

            switch (choice) {
                case "1" -> editObjectiveFlow(resumeId);
                case "2" -> manageSkillsFlow(resumeId); // You would implement these flows
                case "3" -> manageEducationFlow(resumeId);
                case "4" -> manageExperienceFlow(resumeId);
                case "5" -> manageProjectFlow(resumeId);
                case "6" -> manageCertificationFlow(resumeId);
                default -> System.out.println(">>> Invalid Selection.");
            }
        }
    }

    // *********************************************************
    // 2. FOR EMPLOYER: Read-Only View
    // *********************************************************
    public void viewReadOnlyResume(int resumeId, JobSeekerProfile profile, String email) {
        displayResumeStatic(resumeId, profile, email);
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Press Enter to return to Candidate Dossier...");
        scanner.nextLine();
    }

    // *********************************************************
    // 3. SHARED DISPLAY LOGIC (The "Template")
    // *********************************************************
    private void displayResumeStatic(int resumeId, JobSeekerProfile profile, String email) {
        System.out.println("\n\n" + "=".repeat(80));

        // --- 1. HEADER SECTION ---
        System.out.println(centerText(profile.getFullName().toUpperCase(), 80));
        String contactInfo = String.format("%s  |  %s  |  %s", profile.getPhone(), email, profile.getLocation());
        System.out.println(centerText(contactInfo, 80));
        System.out.println("-".repeat(80));

        // --- 2. OBJECTIVE SECTION ---
        System.out.println(centerText("PROFESSIONAL SUMMARY", 80));
        String objective = resumeService.getObjective(resumeId);
        System.out.println(objective != null && !objective.isEmpty() ? objective : "No summary provided.");
        System.out.println("-".repeat(80));

        // --- 3. SKILLS SECTION ---
        System.out.println(centerText("TECHNICAL SKILLS", 80));
        List<Skill> skills = resumeService.getSkillsByResumeId(resumeId);
        String skillLine = skills.stream()
                                 .map(Skill::getSkillName)
                                 .collect(Collectors.joining(" | "));
        System.out.println(centerText(skillLine.isEmpty() ? "No skills added." : skillLine, 80));
        System.out.println("-".repeat(80));

        // --- 4. EDUCATION SECTION ---
        System.out.println(centerText("EDUCATION", 80));
        List<Education> eduList = resumeService.getEducationByResumeId(resumeId);
        if (eduList == null || eduList.isEmpty()) {
            System.out.println(centerText("[No education records]", 80));
        } else {
            for (Education edu : eduList) {
                String years = edu.getStartYear() + " - " + edu.getFormattedEndYear();
                System.out.printf("** %-55s %20s\n", edu.getDegree(), years);
                System.out.println("   " + edu.getInstitution());
            }
        }
        System.out.println("-".repeat(80));

        // --- 5. EXPERIENCE SECTION ---
        System.out.println(centerText("WORK EXPERIENCE (" + profile.getTotalExperience() + " Years Total)", 80));
        List<Experience> expList = resumeService.getExperienceByResumeId(resumeId);
        if (expList == null || expList.isEmpty()) {
            System.out.println(centerText("[No experience records]", 80));
        } else {
            for (Experience e : expList) {
                String end = (e.getEndDate() == null) ? "Present" : e.getEndDate().toString();
                System.out.printf("** %-55s %20s\n", e.getJobRole() + " at " + e.getCompany(), e.getStartDate() + " to " + end);
                System.out.println("   " + e.getDescription());
            }
        }
        System.out.println("-".repeat(80));

        // --- 6. PROJECTS SECTION ---
        System.out.println(centerText("KEY PROJECTS", 80));
        List<Project> projList = resumeService.getProjectsByResumeId(resumeId);
        if (projList == null || projList.isEmpty()) {
            System.out.println(centerText("[No projects added]", 80));
        } else {
            for (Project p : projList) {
                System.out.println("** " + p.getTitle());
                System.out.println("   Tech Stack: " + p.getTechStack());
                System.out.println("   " + p.getDescription());
            }
        }
        System.out.println("-".repeat(80));

        // --- 7. CERTIFICATIONS SECTION ---
        System.out.println(centerText("CERTIFICATIONS", 80));
        List<Certification> certList = resumeService.getCertificationsByResumeId(resumeId);
        if (certList == null || certList.isEmpty()) {
            System.out.println(centerText("[No certifications added]", 80));
        } else {
            for (Certification c : certList) {
                System.out.printf("** %-55s %20s\n", c.getCertName() + " (" + c.getIssuingOrganization() + ")", c.getIssueDate());
            }
        }
    }

//    // --- HELPERS ---
//    private String centerText(String text, int width) {
//        if (text == null || text.length() >= width) return text;
//        int padding = (width - text.length()) / 2;
//        return " ".repeat(padding) + text;
//    }
//
//    private void editObjectiveFlow(int resumeId) {
//        System.out.println("\n--- UPDATE PROFESSIONAL SUMMARY ---");
//        String current = resumeService.getObjective(resumeId);
//        System.out.println("Current: " + (current != null ? current : "None"));
//        System.out.print("Enter New Objective: ");
//        String newObj = scanner.nextLine().trim();
//        if (!newObj.isEmpty() && resumeService.updateObjective(resumeId, newObj)) {
//            System.out.println(">>> Updated!");
//        }
//    }
    
}