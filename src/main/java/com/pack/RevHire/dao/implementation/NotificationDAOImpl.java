package com.pack.RevHire.dao.implementation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pack.RevHire.config.DBConnection;
import com.pack.RevHire.dao.NotificationDAO;
import com.pack.RevHire.model.Notification;

public class NotificationDAOImpl implements NotificationDAO{
	

//	logger
	private static final Logger logger = LogManager.getLogger(DBConnection.class);
	
	
	@Override
	public Map<String, Object> getNotificationContext(int appId) {
	    String sql = "SELECT a.user_id, j.title FROM job_applications a " +
	                 "JOIN jobs j ON a.job_id = j.job_id WHERE a.application_id = ?";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setInt(1, appId);
	        ResultSet rs = ps.executeQuery();
	        if (rs.next()) {
	            return Map.of("user_id", rs.getInt("user_id"), "title", rs.getString("title"));
	        }
	    } catch (SQLException e) { e.printStackTrace(); }
	    return null;
	}
	
	@Override
    public boolean createNotification(int userId, String message) {
        // We only insert user_id and message. 
        // is_read defaults to 0 and created_at defaults to CURRENT_TIMESTAMP in DB.
        String sql = "INSERT INTO notifications (user_id, message, is_read) VALUES (?, ?, 0)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ps.setString(2, message);
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println(">>> SQL Error in createNotification: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
	
	@Override
	public List<Notification> getNotificationsByUserId(int userId) {
	    List<Notification> list = new ArrayList<>();
	    String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setInt(1, userId);
	        ResultSet rs = ps.executeQuery();
	        while (rs.next()) {
	            Notification n = new Notification();
	            n.setNotificationId(rs.getInt("notification_id"));
	            n.setUserId(rs.getInt("user_id"));
	            n.setMessage(rs.getString("message"));
	            n.setRead(rs.getInt("is_read") == 1);
	            n.setCreatedAt(rs.getTimestamp("created_at"));
	            list.add(n);
	        }
	    } catch (SQLException e) { e.printStackTrace(); }
	    return list;
	}

	@Override
	public boolean markAsRead(int notificationId) {
	    String sql = "UPDATE notifications SET is_read = 1 WHERE notification_id = ?";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setInt(1, notificationId);
	        return ps.executeUpdate() > 0;
	    } catch (SQLException e) { return false; }
	}

	@Override
	public boolean deleteNotification(int notificationId) {
	    String sql = "DELETE FROM notifications WHERE notification_id = ?";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setInt(1, notificationId);
	        return ps.executeUpdate() > 0;
	    } catch (SQLException e) { return false; }
	}
	
//	getting details of employer who posted the job
	@Override
	public int getEmployerIdByJobId(int jobId) {
	    // We select employer_id because that is the user_id of the employer in the USERS table
	    String sql = "SELECT employer_id FROM jobs WHERE job_id = ?";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setInt(1, jobId);
	        try (ResultSet rs = ps.executeQuery()) {
	            if (rs.next()) {
	                return rs.getInt("employer_id");
	            }
	        }
	    } catch (SQLException e) {
	        logger.error("Error fetching employer ID for job: " + jobId, e);
	    }
	    return -1; // Indicate failure
	}

	@Override
	public String getJobTitleById(int jobId) {
	    String sql = "SELECT title FROM jobs WHERE job_id = ?";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setInt(1, jobId);
	        ResultSet rs = ps.executeQuery();
	        if (rs.next()) return rs.getString("title");
	    } catch (SQLException e) { e.printStackTrace(); }
	    return "Unknown Job";
	}
	

	@Override
	public boolean bulkMarkAsRead(List<Integer> ids) {
	    // Converts list [1, 2, 3] into "1, 2, 3" for the SQL IN clause
	    String idList = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
	    String sql = "UPDATE notifications SET is_read = 1 WHERE notification_id IN (" + idList + ")";
	    
	    try (Connection conn = DBConnection.getConnection();
	         Statement stmt = conn.createStatement()) {
	        return stmt.executeUpdate(sql) > 0;
	    } catch (SQLException e) {
	        logger.error("Bulk mark read failed", e);
	        return false;
	    }
	}

	@Override
	public boolean bulkDelete(List<Integer> ids) {
	    String idList = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
	    String sql = "DELETE FROM notifications WHERE notification_id IN (" + idList + ")";
	    
	    try (Connection conn = DBConnection.getConnection();
	         Statement stmt = conn.createStatement()) {
	        return stmt.executeUpdate(sql) > 0;
	    } catch (SQLException e) {
	        logger.error("Bulk delete failed", e);
	        return false;
	    }
	}
	
	@Override
	public List<Integer> getMatchingSeekers(int jobId) {
	    List<Integer> userIds = new ArrayList<>();
	    // Corrected SQL: Joining through RESUME to get the USER_ID
	    String sql = "SELECT DISTINCT r.user_id " +
	                 "FROM resume r " +
	                 "JOIN resume_skills rs ON r.resume_id = rs.resume_id " +
	                 "JOIN job_skills js ON rs.skill_id = js.skill_id " +
	                 "WHERE js.job_id = ?";
	    
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setInt(1, jobId);
	        ResultSet rs = ps.executeQuery();
	        while (rs.next()) {
	            userIds.add(rs.getInt("user_id"));
	        }
	    } catch (SQLException e) { 
	        logger.error("Match lookup failed for Job ID: " + jobId, e); 
	    }
	    return userIds;
	}
	
	@Override
	public boolean bulkMarkAsRead(List<Integer> ids, int userId) {
	    if (ids.isEmpty()) return false;
	    String idList = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
	    String sql = "UPDATE notifications SET is_read = 1 WHERE user_id = ? AND notification_id IN (" + idList + ")";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setInt(1, userId);
	        return ps.executeUpdate() > 0;
	    } catch (SQLException e) { return false; }
	}

	@Override
	public boolean bulkDelete(List<Integer> ids, int userId) {
	    if (ids.isEmpty()) return false;
	    String idList = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
	    String sql = "DELETE FROM notifications WHERE user_id = ? AND notification_id IN (" + idList + ")";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setInt(1, userId);
	        return ps.executeUpdate() > 0;
	    } catch (SQLException e) { return false; }
	}

}
