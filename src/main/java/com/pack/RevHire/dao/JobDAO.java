package com.pack.RevHire.dao;

import java.util.List;

import com.pack.RevHire.model.Job;
import com.pack.RevHire.model.Skill;

public interface JobDAO {
	// methods to create new job
    int postJob(Job job);
    boolean addSkillToJob(int jobId, int skillId);
    
    // methods for Management of existing jobs
    List<Job> getJobsByEmployer(int employerId);
    Job getJobById(int jobId);
    List<Skill> getSkillsForJob(int jobId);
    boolean updateJob(Job job);
    boolean deleteJob(int jobId);
    int getApplicantCount(int jobId);
    boolean changeStatus(int jobId, String status); 
    boolean removeAllSkillsFromJob(int jobId);
}