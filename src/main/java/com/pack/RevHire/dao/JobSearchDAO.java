package com.pack.RevHire.dao;

import com.pack.RevHire.model.Job;
import java.util.List;

public interface JobSearchDAO {
    List<Job> getAllJobs();
    List<Job> searchByTitle(String title);
    List<Job> searchBySkills(List<Integer> skillIds);
    boolean hasUserApplied(int userId, int jobId);
    List<Job> searchByFilters(String role, String location, Integer exp, String company, Double minSal, String type);
}