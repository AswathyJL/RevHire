package com.pack.RevHire.dao.implementation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
//import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.pack.RevHire.config.DBConnection;
import com.pack.RevHire.dao.JobApplicationDAO;
import com.pack.RevHire.model.JobApplication;

public class JobApplicationDAOImpl implements JobApplicationDAO {

    @Override
    public boolean saveApplication(JobApplication app) {
        String sql = "INSERT INTO job_applications (job_id, user_id, cover_letter) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, app.getJobId());
            pstmt.setInt(2, app.getUserId());
            pstmt.setString(3, app.getCoverLetter());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public List<Map<String, Object>> getApplicationsByUserId(int userId) {
        String sql = "SELECT a.application_id, a.job_id, j.title, a.status, " +
                     "a.employer_comments, a.applied_at " +
                     "FROM job_applications a JOIN jobs j ON a.job_id = j.job_id " +
                     "WHERE a.user_id = ? ORDER BY a.applied_at DESC";
        return executeQuery(sql, userId);
    }

    @Override
    public boolean updateStatus(int appId, String status, String reason) {
        String sql = "UPDATE job_applications SET status = ?, withdrawal_reason = ? WHERE application_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setString(2, reason);
            pstmt.setInt(3, appId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean deleteApplication(int appId) {
        String sql = "DELETE FROM job_applications WHERE application_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, appId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // --- Employer Specific Methods ---

    @Override
    public List<Map<String, Object>> getJobStatsByEmployer(int employerId) {
        // Ensuring Aliases match the lowercase keys expected by your View
        String sql = "SELECT j.job_id AS job_id, j.title AS title, COUNT(a.application_id) AS applicant_count " +
                     "FROM jobs j LEFT JOIN job_applications a ON j.job_id = a.job_id " +
                     "WHERE j.employer_id = ? GROUP BY j.job_id, j.title";
        return executeQuery(sql, employerId);
    }

    @Override
	public List<Map<String, Object>> getApplicantsByJobId(int jobId) {
	    // We use LEFT JOIN so that even if the 'users' or 'profile' record 
	    // is slightly out of sync, the application record itself still shows up.
	    String sql = "SELECT a.application_id, a.user_id, p.full_name AS name, " +
	                 "u.email, a.status, a.applied_at " +
	                 "FROM job_applications a " +
	                 "LEFT JOIN job_seeker_profile p ON a.user_id = p.user_id " +
	                 "LEFT JOIN users u ON a.user_id = u.user_id " +
	                 "WHERE a.job_id = ?";
	    
	    return executeQuery(sql, jobId);
	}
	   
    @Override
    public boolean updateStatusAndComments(int appId, String status, String comments) {
        String sql = "UPDATE job_applications SET status = ?, employer_comments = ? WHERE application_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setString(2, comments);
            pstmt.setInt(3, appId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
	public List<Map<String, Object>> filterApplicantsBySkills(int jobId, List<Integer> skillIds) {
	    String placeholders = skillIds.stream().map(s -> "?").collect(Collectors.joining(","));
	
	    // FIXED: Using resume_skills and joining through the resume table
	    String sql = "SELECT DISTINCT a.application_id, u.user_id, p.full_name AS name, u.email, a.status, a.applied_at " +
	                 "FROM job_applications a " +
	                 "JOIN users u ON a.user_id = u.user_id " +
	                 "JOIN job_seeker_profile p ON u.user_id = p.user_id " +
	                 "JOIN resume r ON u.user_id = r.user_id " + // Need to link user to resume
	                 "JOIN resume_skills rs ON r.resume_id = rs.resume_id " + // Use actual table name
	                 "WHERE a.job_id = ? AND rs.skill_id IN (" + placeholders + ")";
	    
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {
	        
	        ps.setInt(1, jobId);
	        for (int i = 0; i < skillIds.size(); i++) {
	            ps.setInt(i + 2, skillIds.get(i));
	        }
	        
	        return mapResultSet(ps.executeQuery());
	    } catch (SQLException e) { 
	        e.printStackTrace(); 
	        return new ArrayList<>(); 
	    }
	}

    @Override
	public List<Map<String, Object>> globalSkillSearch(List<Integer> skillIds) {
	    List<Map<String, Object>> candidates = new ArrayList<>();
	    if (skillIds == null || skillIds.isEmpty()) return candidates;
	
	    String placeholders = skillIds.stream().map(s -> "?").collect(Collectors.joining(","));
	    
	    // Updated to match your schema: Resume -> Resume_Skills
	    // We join p.user_id to u.user_id based on your FK constraints.
	    String sql = "SELECT p.user_id, u.email, p.full_name AS name, p.location " +
	                 "FROM resume_skills rs " +
	                 "JOIN resume r ON rs.resume_id = r.resume_id " +
	                 "JOIN job_seeker_profile p ON r.user_id = p.user_id " +
	                 "JOIN users u ON p.user_id = u.user_id " +
	                 "WHERE rs.skill_id IN (" + placeholders + ") " +
	                 "GROUP BY p.user_id, u.email, p.full_name, p.location " +
	                 "HAVING COUNT(DISTINCT rs.skill_id) = ?";
	
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {
	        
	        int index = 1;
	        for (Integer id : skillIds) {
	            ps.setInt(index++, id);
	        }
	        ps.setInt(index, skillIds.size());
	
	        try (ResultSet rs = ps.executeQuery()) {
	            while (rs.next()) {
	                Map<String, Object> row = new HashMap<>();
	                row.put("user_id", rs.getInt("user_id"));
	                row.put("name", rs.getString("name"));
	                row.put("email", rs.getString("email"));
	                row.put("location", rs.getString("location"));
	                candidates.add(row);
	            }
	        }
	    } catch (SQLException e) {
	        // This will now catch if 'resume' or 'resume_skills' table is missing
	        System.err.println("Search Error: " + e.getMessage());
	    }
	    return candidates;
	}
    // --- Helper Methods ---

    private List<Map<String, Object>> executeQuery(String sql, int param) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, param);
            return mapResultSet(ps.executeQuery());
        } catch (SQLException e) { e.printStackTrace(); return new ArrayList<>(); }
    }

    private List<Map<String, Object>> mapResultSet(ResultSet rs) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                // Key is converted to lowercase to avoid "null" issues in View
                row.put(metaData.getColumnLabel(i).toLowerCase(), rs.getObject(i));
            }
            list.add(row);
        }
        return list;
    }
}