package com.pack.RevHire.service;

import com.pack.RevHire.dao.JobDAO;
import com.pack.RevHire.dao.NotificationDAO;
import com.pack.RevHire.dao.implementation.JobDAOImpl;
import com.pack.RevHire.dao.implementation.NotificationDAOImpl;
import com.pack.RevHire.model.Job;
import com.pack.RevHire.model.Skill; // Added missing import
import java.time.LocalDate;
import java.util.List;

public class JobService {
    private JobDAO jobDAO = new JobDAOImpl();
    private NotificationDAO notificationDAO = new NotificationDAOImpl();

    /**
     * Creates a job and links the provided skill IDs.
     * Validates all inputs before calling the DAO.
     */
    public int createJob(Job job, List<Integer> skillIds) {
        // 1. Mandatory Field Validation
        if (job.getTitle() == null || job.getTitle().trim().isEmpty()) {
            System.out.println(">>> Validation Error: Job Title is required.");
            return -1;
        }

        if (job.getDeadline() == null) {
            System.out.println(">>> Validation Error: Application Deadline is required.");
            return -1;
        }

        // 2. Date Validation (Deadline must be today or in the future)
        LocalDate today = LocalDate.now();
        if (job.getDeadline().toLocalDate().isBefore(today)) {
            System.out.println(">>> Validation Error: Deadline cannot be in the past.");
            return -1;
        }

        // 3. Salary Validation
        if (job.getSalaryMax() < job.getSalaryMin()) {
            System.out.println(">>> Validation Error: Max Salary must be greater than or equal to Min Salary.");
            return -1;
        }

        // 4. Skills Validation
        if (skillIds == null || skillIds.isEmpty()) {
            System.out.println(">>> Validation Error: At least one required skill must be added.");
            return -1;
        }

        // --- All Validations Passed ---
        List<Integer> distinctSkillIds = skillIds.stream().distinct().toList();
        // Save the Job Header first (Returns generated ID)
        int generatedJobId = jobDAO.postJob(job);
        
        if (generatedJobId != -1) {
            for (Integer skillId : distinctSkillIds) {
                // DAO handles the insertion into job_skills
                jobDAO.addSkillToJob(generatedJobId, skillId);
            }
//            trigger to match notification
            List<Integer> matchingSeekers = notificationDAO.getMatchingSeekers(generatedJobId);
            String message = "Job Match: A new " + job.getTitle() + " role matches your skills!";
            for (Integer seekerId : matchingSeekers) {
                notificationDAO.createNotification(seekerId, message);
            }
            return generatedJobId;
        }

        return -1;
    }

   

    /**
     * Links a single skill to a job. Useful for job updates.
     */
    public boolean linkSkillToJob(int jobId, int skillId) {
        return jobDAO.addSkillToJob(jobId, skillId);
    }
    
    /**
     * Fetches all jobs posted by a specific employer.
     * Used for the initial "Dashboard" table view.
     */
    public List<Job> getEmployerJobs(int employerId) {
        // Calling DAO to get the summary list
        return jobDAO.getJobsByEmployer(employerId);
    }

    /**
     * Retrieves a single Job object by its ID.
     * Used when the employer selects a specific job to view details or edit.
     */
    public Job getJobById(int jobId) {
        Job job = jobDAO.getJobById(jobId);
        if (job == null) {
            System.out.println(">>> Error: Job with ID " + jobId + " not found.");
        }
        return job;
    }

    /**
     * Retrieves the technical skills associated with a specific job.
     * This performs the join logic through the DAO.
     */
    public List<Skill> getSkillsByJobId(int jobId) {
        return jobDAO.getSkillsForJob(jobId);
    }

    /**
     * Updates an existing job's details.
     * Validates basic logic before committing to the database.
     */
    public boolean updateJobDetails(Job job) {
        // Basic business rule validation
        if (job.getSalaryMax() < job.getSalaryMin()) {
            System.out.println(">>> Validation Error: Max Salary cannot be less than Min Salary.");
            return false;
        }
        
        // Check if deadline is not in the past (Optional for updates)
        if (job.getDeadline().toLocalDate().isBefore(java.time.LocalDate.now())) {
            System.out.println(">>> Warning: Job deadline is set in the past.");
        }

        return jobDAO.updateJob(job);
    }
    
    public int getApplicantCount(int jobId) {
        return jobDAO.getApplicantCount(jobId);
    }

    public boolean updateJobStatus(int jobId, String status) {
        return jobDAO.changeStatus(jobId, status);
    }

    /**
     * Permanently removes a job.
     * The DAO implementation must handle the deletion of linked skills 
     * in the junction table first to avoid SQL errors.
     */
    public boolean removeJob(int jobId) {
        if (jobId <= 0) return false;
        
        boolean isDeleted = jobDAO.deleteJob(jobId);
        if (!isDeleted) {
            System.out.println(">>> Error: System could not delete the job.");
        }
        return isDeleted;
    }
    
    
    /**
     * Comprehensive update for a job and its associated skills.
     * If skillIds is null, it only updates job details.
     */
    public boolean updateJobWithSkills(Job job, List<Integer> skillIds) {
        // 1. First, update the basic job details (Title, Salary, etc.)
        boolean jobInfoUpdated = updateJobDetails(job);
        
        if (!jobInfoUpdated) {
            return false;
        }

        // 2. If the user chose to update skills (skillIds is not null)
        if (skillIds != null) {
            if (skillIds.isEmpty()) {
                System.out.println(">>> Validation Error: At least one skill is required.");
                return false;
            }

            // --- Logic: Sync the Junction Table ---
            // Step A: Remove all old skills for this job
            jobDAO.removeAllSkillsFromJob(job.getJobId());

            // Step B: Add the new skills
            List<Integer> distinctSkillIds = skillIds.stream().distinct().toList();
            for (Integer skillId : distinctSkillIds) {
                jobDAO.addSkillToJob(job.getJobId(), skillId);
            }
        }

        return true;
    }

    
    
}