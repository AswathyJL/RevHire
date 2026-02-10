package com.pack.RevHire.dao;

import java.util.List;
import java.util.Map;

import com.pack.RevHire.model.JobApplication;

public interface JobApplicationDAO {
	boolean saveApplication(JobApplication app);
	/**
     * Fetches application details joined with Job Title for the Seeker's Dashboard.
     * Returns a Map for easy tabular display without creating a new DTO.
     */
    List<Map<String, Object>> getApplicationsByUserId(int userId);

    /**
     * Updates the status of an application (e.g., to 'WITHDRAWN').
     * Also allows setting a withdrawal reason.
     */
    boolean updateStatus(int appId, String status, String reason);

    /**
     * Permanently removes a record from the database.
     * Rule: Only for 'REJECTED' or 'WITHDRAWN' statuses.
     */
    boolean deleteApplication(int appId);
    
 // Employer methods
    List<Map<String, Object>> getJobStatsByEmployer(int employerId);
    List<Map<String, Object>> getApplicantsByJobId(int jobId);
    boolean updateStatusAndComments(int appId, String status, String comments);
    List<Map<String, Object>> filterApplicantsBySkills(int jobId, List<Integer> skillIds);
    List<Map<String, Object>> globalSkillSearch(List<Integer> skillIds);
}
