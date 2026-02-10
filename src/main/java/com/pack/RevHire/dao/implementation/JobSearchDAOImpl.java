package com.pack.RevHire.dao.implementation;

import com.pack.RevHire.config.DBConnection;
import com.pack.RevHire.dao.JobSearchDAO;
import com.pack.RevHire.model.Job;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class JobSearchDAOImpl implements JobSearchDAO {

    // BASE_QUERY ensures we always join with employer_profile to get the company name
    private final String BASE_QUERY = "SELECT j.*, e.company_name FROM jobs j " +
                                     "JOIN employer_profile e ON j.employer_id = e.user_id ";

    @Override
    public List<Job> getAllJobs() {
        // Changed from "SELECT * FROM jobs" to use the JOIN constant
        return fetchJobs(BASE_QUERY + "WHERE j.status = 'OPEN'");
    }

    @Override
    public List<Job> searchByTitle(String title) {
        // Updated to use JOIN so company name isn't null
        return fetchJobs(BASE_QUERY + "WHERE j.status = 'OPEN' AND LOWER(j.title) LIKE '%" + title.toLowerCase() + "%'");
    }

    @Override
    public List<Job> searchBySkills(List<Integer> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) return new ArrayList<>();
        
        String ids = skillIds.stream()
                             .map(String::valueOf)
                             .collect(Collectors.joining(",", "(", ")"));

        // Updated to use JOIN
        String sql = BASE_QUERY + "WHERE j.status = 'OPEN' AND j.job_id IN (" +
                     "SELECT DISTINCT job_id FROM job_skills WHERE skill_id IN " + ids + ")";
        
        return fetchJobs(sql);
    }
    
    @Override
    public List<Job> searchByFilters(String role, String location, Integer exp, String company, Double minSal, String type) {
        StringBuilder sql = new StringBuilder(BASE_QUERY);
        sql.append(" WHERE j.status = 'OPEN'");

        if (role != null && !role.isEmpty()) {
            sql.append(" AND LOWER(j.title) LIKE '%").append(role.toLowerCase()).append("%'");
        }
        if (location != null && !location.isEmpty()) {
            sql.append(" AND LOWER(j.location) LIKE '%").append(location.toLowerCase()).append("%'");
        }
        if (exp != null) {
            sql.append(" AND j.experience_required <= ").append(exp);
        }
        if (company != null && !company.isEmpty()) {
            sql.append(" AND LOWER(e.company_name) LIKE '%").append(company.toLowerCase()).append("%'");
        }
        if (minSal != null) {
            sql.append(" AND j.salary_max >= ").append(minSal);
        }
        if (type != null && !type.isEmpty()) {
            sql.append(" AND j.job_type = '").append(type).append("'");
        }

        return fetchJobs(sql.toString());
    }

    private List<Job> fetchJobs(String sql) {
        List<Job> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Job j = new Job();
                j.setJobId(rs.getInt("job_id"));
                j.setTitle(rs.getString("title"));
                j.setLocation(rs.getString("location"));
                j.setJobType(rs.getString("job_type"));
                j.setSalaryMin(rs.getDouble("salary_min"));
                j.setSalaryMax(rs.getDouble("salary_max"));
                j.setDeadline(rs.getDate("deadline"));
                j.setDescription(rs.getString("description"));
                
                // CRITICAL FIX: Extract company_name from the ResultSet and set it in the Job object
                // If this line is missing, your View will always show "N/A"
                j.setCompanyName(rs.getString("company_name")); 
                
                list.add(j);
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return list;
    }
    
    @Override
    public boolean hasUserApplied(int userId, int jobId) {
        String sql = "SELECT COUNT(*) FROM job_applications WHERE user_id = ? AND job_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, jobId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return false;
    }
}