package com.pack.RevHire.service;

import java.util.List;

import com.pack.RevHire.dao.ResumeDAO;
import com.pack.RevHire.dao.implementation.ResumeDAOimpl;
import com.pack.RevHire.model.Certification;
import com.pack.RevHire.model.Education;
import com.pack.RevHire.model.Experience;
import com.pack.RevHire.model.Project;
import com.pack.RevHire.model.Skill;

public class ResumeService {
    private ResumeDAO resumeDAO = new ResumeDAOimpl();

    /**
     * The Gateway Method: 
     * Checks if a resume exists for the user. If not, creates one.
     * Returns the resumeId for use in sub-sections.
     */
    public int getOrCreateResumeId(int userId) {
        Integer resumeId = resumeDAO.getResumeIdByUserId(userId);
        
        if (resumeId == null) {
            // No resume found, create a new entry for this user
            System.out.println(">>> Initializing new resume record...");
            return resumeDAO.createResume(userId);
        }
        
        return resumeId;
    }
    
//  *********************************************************
//  OBJECTIVE
//*********************************************************

    public String getObjective(int resumeId) {
        return resumeDAO.getObjective(resumeId);
    }

    public boolean updateObjective(int resumeId, String objective) {
        return resumeDAO.updateObjective(resumeId, objective);
    }

// 	  ********************************************************   
//    SKILLS
//    *********************************************************
    
    /**
     * Fetches only the skills linked to a specific resume.
     */
    public List<Skill> getSkillsByResumeId(int resumeId) {
        return resumeDAO.getSkillsByResumeId(resumeId);
    }

    /**
     * Links a skill ID to a resume ID in the junction table.
     */
    public boolean addSkillToResume(int resumeId, int skillId) {
        // Validation: ensure IDs are positive
        if (resumeId <= 0 || skillId <= 0) return false;
        
        return resumeDAO.linkSkillToResume(resumeId, skillId);
    }
    
    public boolean removeSkillFromResume(int resumeId, int skillId) {
        if (resumeId <= 0 || skillId <= 0) return false;
        return resumeDAO.unlinkSkillFromResume(resumeId, skillId);
    }
    
//  *********************************************************
//    EDUCATION
//  *********************************************************
    /**
     * Retrieves all education records for a resume.
     * The DAO handles the descending sort by end_year.
     */
    public List<Education> getEducationByResumeId(int resumeId) {
        if (resumeId <= 0) return null;
        return resumeDAO.getEducationByResumeId(resumeId);
    }

    /**
     * Fetches a single education record by ID.
     * Used specifically for the Edit Flow.
     */
    public Education getEducationById(int eduId) {
        if (eduId <= 0) return null;
        return resumeDAO.getEducationById(eduId);
    }

    /**
     * Adds a new education entry.
     * Validates that the timeline makes sense before saving.
     */
    public boolean addEducation(Education edu) {
        if (edu.getEndYear() != 0 && edu.getStartYear() > edu.getEndYear()) {
            System.out.println(">>> Validation Error: Start year cannot be after End year.");
            return false;
        }
        if (edu.getDegree() == null || edu.getDegree().isEmpty()) {
            return false;
        }
        return resumeDAO.insertEducation(edu);
    }

    /**
     * Updates an existing education entry.
     */
    public boolean updateEducation(Education edu) {
        if (edu.getEducationId() <= 0) return false;
        return resumeDAO.updateEducation(edu);
    }

    /**
     * Permanently removes an education entry.
     */
    public boolean deleteEducation(int eduId) {
        if (eduId <= 0) return false;
        return resumeDAO.deleteEducation(eduId);
    }
    
//  *********************************************************
//  EXPERIENCE
//	*********************************************************
    
    /**
     * Retrieves all experience for a resume.
     * The sorting (Present first, then descending date) is handled by the DAO.
     */
    public List<Experience> getExperienceByResumeId(int resumeId) {
        if (resumeId <= 0) return null;
        return resumeDAO.getExperienceByResumeId(resumeId);
    }

    /**
     * Fetches a specific experience entry.
     * Crucial for the 'Edit' flow to load existing data.
     */
    public Experience getExperienceById(int expId) {
        if (expId <= 0) return null;
        return resumeDAO.getExperienceById(expId);
    }

    /**
     * Adds a new work experience entry.
     * Enforces the business rule: Start date must be before End date.
     */
    public boolean addExperience(Experience exp) {
        // Validation: If endDate is provided (not null), check it against startDate
        if (exp.getEndDate() != null && exp.getEndDate().before(exp.getStartDate())) {
            System.out.println(">>> Validation Error: End date cannot be before start date.");
            return false;
        }
        
        // Basic requirement check
        if (exp.getCompany() == null || exp.getCompany().isEmpty()) return false;
        
        return resumeDAO.insertExperience(exp);
    }

    /**
     * Updates an existing experience entry.
     */
    public boolean updateExperience(Experience exp) {
        if (exp.getExperienceId() <= 0) return false;
        
        // Re-validate dates in case they were modified during edit
        if (exp.getEndDate() != null && exp.getEndDate().before(exp.getStartDate())) {
            return false;
        }
        
        return resumeDAO.updateExperience(exp);
    }

    /**
     * Deletes an experience entry.
     */
    public boolean deleteExperience(int expId) {
        if (expId <= 0) return false;
        return resumeDAO.deleteExperience(expId);
    }
    
    
    public List<Project> getProjectsByResumeId(int resumeId) {
        return resumeDAO.getProjectsByResumeId(resumeId);
    }

//  *********************************************************
//  PROJECT
//	*********************************************************
    
    public Project getProjectById(int projectId) {
        return resumeDAO.getProjectById(projectId);
    }

    public boolean addProject(Project project) {
        // Validation: Title is mandatory
        if (project.getTitle() == null || project.getTitle().trim().isEmpty()) {
            System.out.println(">>> Validation Error: Project title is required.");
            return false;
        }
        
        // Clean up tech stack string (remove trailing commas if any)
        if (project.getTechStack() != null) {
            String cleaned = project.getTechStack().replaceAll(", $", "");
            project.setTechStack(cleaned);
        }
        
        return resumeDAO.insertProject(project);
    }

    public boolean updateProject(Project project) {
        // Ensures we aren't updating a non-existent project
        if (project.getProjectId() <= 0) return false;
        return resumeDAO.updateProject(project);
    }

    public boolean deleteProject(int projectId) {
        return resumeDAO.deleteProject(projectId);
    }
    
//  *********************************************************
//  CERTIFICATION
//	*********************************************************
    
    public List<Certification> getCertificationsByResumeId(int resumeId) {
        return resumeDAO.getCertificationsByResumeId(resumeId);
    }

    public Certification getCertificationById(int certId) {
        return resumeDAO.getCertificationById(certId);
    }

    public boolean addCertification(Certification cert) {
        if (cert.getCertName().isEmpty()) return false;
        return resumeDAO.insertCertification(cert);
    }

    public boolean updateCertification(Certification cert) {
        return resumeDAO.updateCertification(cert);
    }

    public boolean deleteCertification(int certId) {
        return resumeDAO.deleteCertification(certId);
    }
    
//  *********************************************************
//  VIEW FULL RESUME
//	*********************************************************
    
}