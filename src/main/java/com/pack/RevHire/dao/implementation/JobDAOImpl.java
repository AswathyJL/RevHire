package com.pack.RevHire.dao.implementation;

import com.pack.RevHire.dao.JobDAO;
import com.pack.RevHire.model.Job;
import com.pack.RevHire.model.Skill;
import com.pack.RevHire.config.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JobDAOImpl implements JobDAO {

    @Override
    public int postJob(Job job) {
        String sql = "INSERT INTO jobs (employer_id, title, description, experience_required, " +
                     "education_required, location, salary_min, salary_max, job_type, deadline) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[] { "JOB_ID" })) {
            
            stmt.setInt(1, job.getEmployerId());
            stmt.setString(2, job.getTitle());
            stmt.setString(3, job.getDescription());
            stmt.setInt(4, job.getExperienceRequired());
            stmt.setString(5, job.getEducationRequired());
            stmt.setString(6, job.getLocation());
            stmt.setDouble(7, job.getSalaryMin());
            stmt.setDouble(8, job.getSalaryMax());
            stmt.setString(9, job.getJobType());
            stmt.setDate(10, job.getDeadline());

            if (stmt.executeUpdate() > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Database Error in postJob: " + e.getMessage());
        }
        return -1;
    }

    @Override
    public boolean addSkillToJob(int jobId, int skillId) {
        String sql = "INSERT INTO job_skills (job_id, skill_id) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, jobId);
            stmt.setInt(2, skillId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Database Error in addSkillToJob: " + e.getMessage());
            return false;
        }
    }

    
    @Override
    public List<Job> getJobsByEmployer(int employerId) {
        List<Job> jobs = new ArrayList<>();
        // Ordered by deadline DESC as requested
        String sql = "SELECT job_id, title, deadline FROM jobs WHERE employer_id = ? ORDER BY deadline DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, employerId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Job job = new Job();
                job.setJobId(rs.getInt("job_id"));
                job.setTitle(rs.getString("title"));
                job.setDeadline(rs.getDate("deadline"));
                jobs.add(job);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return jobs;
    }

   @Override
	public Job getJobById(int jobId) {
	    String sql = "SELECT j.*, e.company_name FROM jobs j " +
	                 "LEFT JOIN employer_profile e ON j.employer_id = e.user_id " +
	                 "WHERE j.job_id = ?";
	    
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setInt(1, jobId);
	        ResultSet rs = ps.executeQuery();
	        
	        if (rs.next()) {
	            Job j = new Job();
	            j.setJobId(rs.getInt("job_id"));
	            
	            // ADD THIS LINE - This is why the Employer flow is failing!
	            j.setEmployerId(rs.getInt("employer_id")); 
	            
	            j.setTitle(rs.getString("title"));
	            j.setLocation(rs.getString("location"));
	            j.setJobType(rs.getString("job_type"));
	            j.setSalaryMin(rs.getDouble("salary_min"));
	            j.setSalaryMax(rs.getDouble("salary_max"));
	            j.setDeadline(rs.getDate("deadline"));
	            j.setDescription(rs.getString("description"));
	            j.setStatus(rs.getString("status")); 
	            j.setCompanyName(rs.getString("company_name"));
	            
	            return j;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null;
	}

    @Override
    public List<Skill> getSkillsForJob(int jobId) {
        List<Skill> skills = new ArrayList<>();
        // SQL JOIN to bridge Job and Skill via the junction table
        String sql = "SELECT s.skill_id, s.skill_name FROM skills s " +
                     "JOIN job_skills js ON s.skill_id = js.skill_id " +
                     "WHERE js.job_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, jobId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Skill skill = new Skill();
                skill.setSkillId(rs.getInt("skill_id"));
                skill.setSkillName(rs.getString("skill_name"));
                skills.add(skill);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return skills;
    }

    @Override
    public boolean updateJob(Job job) {
        String sql = "UPDATE jobs SET title = ?, location = ?, salary_min = ?, " +
                     "salary_max = ?, deadline = ?, description = ?, experience_required = ? " +
                     "WHERE job_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, job.getTitle());
            pstmt.setString(2, job.getLocation());
            pstmt.setDouble(3, job.getSalaryMin());
            pstmt.setDouble(4, job.getSalaryMax());
            pstmt.setDate(5, job.getDeadline());
            pstmt.setString(6, job.getDescription());
            pstmt.setInt(7, job.getExperienceRequired());
            pstmt.setInt(8, job.getJobId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int getApplicantCount(int jobId) {
        // Change 'applications' to 'job_applications'
        String sql = "SELECT COUNT(*) FROM job_applications WHERE job_id = ?"; 
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, jobId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean changeStatus(int jobId, String status) {
        String sql = "UPDATE jobs SET status = ? WHERE job_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, jobId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean deleteJob(int jobId) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin Transaction

            // 1. Delete associated skills in the junction table first
            String deleteSkillsSql = "DELETE FROM job_skills WHERE job_id = ?";
            try (PreparedStatement ps1 = conn.prepareStatement(deleteSkillsSql)) {
                ps1.setInt(1, jobId);
                ps1.executeUpdate();
            }

            // 2. Delete the job record
            String deleteJobSql = "DELETE FROM jobs WHERE job_id = ?";
            try (PreparedStatement ps2 = conn.prepareStatement(deleteJobSql)) {
                ps2.setInt(1, jobId);
                ps2.executeUpdate();
            }

            conn.commit(); // End Transaction
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
    
    @Override
    public boolean removeAllSkillsFromJob(int jobId) {
        String sql = "DELETE FROM job_skills WHERE job_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, jobId);
            return pstmt.executeUpdate() >= 0; // Returns true even if 0 rows were deleted
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    
}