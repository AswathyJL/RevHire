package com.pack.RevHire.service;

import com.pack.RevHire.dao.JobSearchDAO;
import com.pack.RevHire.dao.implementation.JobSearchDAOImpl;
import com.pack.RevHire.model.*;
import java.util.List;
import java.util.stream.Collectors;

public class JobSearchService {
    private JobSearchDAO searchDAO = new JobSearchDAOImpl();
    private JobService jobService = new JobService(); // Used only for skill names and ID lookups

    public List<Job> getAllJobs() { return searchDAO.getAllJobs(); }
    
    public List<Job> searchByTitle(String title) { return searchDAO.searchByTitle(title); }
    
    public List<Job> searchBySkills(List<Integer> ids) { return searchDAO.searchBySkills(ids); }

    public String getFormattedSkills(int jobId) {
        return jobService.getSkillsByJobId(jobId).stream()
                .map(Skill::getSkillName)
                .collect(Collectors.joining(", "));
    }
    
    public boolean isApplied(int userId, int jobId) {
        return searchDAO.hasUserApplied(userId, jobId);
    }
    
    public List<Job> searchByFilters(String role, String location, Integer exp, String company, Double minSal, String type) {
        return searchDAO.searchByFilters(role, location, exp, company, minSal, type);
    }
    
    
}