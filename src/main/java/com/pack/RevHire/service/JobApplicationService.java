package com.pack.RevHire.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pack.RevHire.config.DBConnection;
import com.pack.RevHire.dao.JobApplicationDAO;
import com.pack.RevHire.dao.NotificationDAO;
import com.pack.RevHire.dao.implementation.JobApplicationDAOImpl;
import com.pack.RevHire.dao.implementation.NotificationDAOImpl;
import com.pack.RevHire.model.JobApplication;

public class JobApplicationService {
	
//	logger
	private static final Logger logger = LogManager.getLogger(DBConnection.class);
	

	private JobApplicationDAO appDAO = new JobApplicationDAOImpl();
	private NotificationDAO notificationDAO = new NotificationDAOImpl();

   public boolean submitApplication(int jobId, int userId, String coverLetter) {
	   JobApplication app = new JobApplication();
	    app.setJobId(jobId);
	    app.setUserId(userId);
	    app.setCoverLetter(coverLetter);
    // 1. Save the actual application
    boolean isSaved = appDAO.saveApplication(app);

    if (isSaved) {
        // 2. Fetch the Employer ID to notify
        int employerId = notificationDAO.getEmployerIdByJobId(jobId);
        String jobTitle = notificationDAO.getJobTitleById(jobId);

        if (employerId != -1) {
            String msg = "New Application: A candidate has applied for your role: " + jobTitle;
            notificationDAO.createNotification(employerId, msg);
            logger.info("Employer {} notified for Job {}", employerId, jobId);
        } else {
            logger.warn("Application saved but Employer ID not found for Job {}", jobId);
        }
    }
    return isSaved;
}
	    
    public List<Map<String, Object>> getMyApplications(int userId) {
        return appDAO.getApplicationsByUserId(userId);
    }

    public boolean withdrawApplication(int appId, String reason) {
        return appDAO.updateStatus(appId, "WITHDRAWN", reason);
    }

    public boolean deleteApplication(int appId) {
        return appDAO.deleteApplication(appId);
    }
    
//    employer job application search and filter
    
    /**
     * Fetches job statistics for an employer: Job Title, ID, and number of applicants.
     */
    public List<Map<String, Object>> getJobStats(int employerId) {
        return appDAO.getJobStatsByEmployer(employerId);
    }

    /**
     * Retrieves all applicants for a specific job posting.
     */
    public List<Map<String, Object>> getApplicantsForJob(int jobId) {
        return appDAO.getApplicantsByJobId(jobId);
    }

    /**
     * Updates application status (SHORTLISTED/REJECTED) and adds employer feedback.
     */
    public boolean updateApplicationByEmployer(int appId, String status, String comments) {
        if (appId <= 0 || status == null) return false;

        // 1. Perform the original update
        boolean isUpdated = appDAO.updateStatusAndComments(appId, status, comments);

        // 2. Add Notification Logic (Silent failure to not affect main flow)
        if (isUpdated) {
            try {
                Map<String, Object> context = notificationDAO.getNotificationContext(appId);
                if (context != null) {
                    int seekerId = (int) context.get("user_id");
                    String jobTitle = (String) context.get("title");
                    
                    String message = String.format("Application Update: You have been %s for the position '%s'.", 
                                                    status, jobTitle);
                    
                    notificationDAO.createNotification(seekerId, message);
                }
            } catch (Exception e) {
                // Log error but don't stop the employer's process
                System.err.println(">>> Notification failed: " + e.getMessage());
            }
        }

        return isUpdated;
    }

    /**
     * Filters applicants of a specific job based on a list of skill IDs.
     */
    public List<Map<String, Object>> getApplicantsBySkills(int jobId, List<Integer> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) {
            return appDAO.getApplicantsByJobId(jobId);
        }
        return appDAO.filterApplicantsBySkills(jobId, skillIds);
    }

    /**
     * Global search: Finds any job seeker in the system who has the required skills.
     */
    public List<Map<String, Object>> searchCandidatesBySkills(List<Integer> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) return new ArrayList<>();
        return appDAO.globalSkillSearch(skillIds);
    }
    
}
