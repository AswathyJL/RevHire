package com.pack.RevHire.dao;

import java.util.List;

import com.pack.RevHire.model.Certification;
import com.pack.RevHire.model.Education;
import com.pack.RevHire.model.Experience;
import com.pack.RevHire.model.Project;
import com.pack.RevHire.model.Skill;

public interface ResumeDAO {
	
	// Check if resume exists and return its ID
    Integer getResumeIdByUserId(int userId);
    
    // Create initial resume record and return generated ID
    int createResume(int userId);
    
    // Objective Management
    boolean updateObjective(int resumeId, String objective);
    String getObjective(int resumeId);
    
 // Skill Linking 
    List<Skill> getSkillsByResumeId(int resumeId);
    boolean linkSkillToResume(int resumeId, int skillId);
    boolean unlinkSkillFromResume(int resumeId, int skillId);
    
//    EDUCATION
    List<Education> getEducationByResumeId(int resumeId);
    Education getEducationById(int eduId);
    boolean insertEducation(Education edu);
    boolean updateEducation(Education edu);
    boolean deleteEducation(int eduId);
    
 // Experience Management
    List<Experience> getExperienceByResumeId(int resumeId);
    Experience getExperienceById(int expId);
    boolean insertExperience(Experience exp);
    boolean updateExperience(Experience exp);
    boolean deleteExperience(int expId);
    
//    PROJECTS
    List<Project> getProjectsByResumeId(int resumeId);
    Project getProjectById(int projectId);
    boolean insertProject(Project project);
    boolean updateProject(Project project);
    boolean deleteProject(int projectId);
    
//    CERTIFICATION
    List<Certification> getCertificationsByResumeId(int resumeId);
    Certification getCertificationById(int certId);
    boolean insertCertification(Certification cert);
    boolean updateCertification(Certification cert);
    boolean deleteCertification(int certId);
}
