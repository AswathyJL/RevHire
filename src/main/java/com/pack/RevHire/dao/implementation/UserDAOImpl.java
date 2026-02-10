package com.pack.RevHire.dao.implementation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.pack.RevHire.config.DBConnection;
import com.pack.RevHire.dao.UserDAO;
import com.pack.RevHire.model.EmployerProfile;
import com.pack.RevHire.model.JobSeekerProfile;
import com.pack.RevHire.model.User;

public class UserDAOImpl implements UserDAO 
{

	@Override
	public boolean registerUser(User user) {
		String sql = "INSERT INTO users (email, password, role, security_question, security_answer) VALUES (?,?,?,?,?)";
//		? are placeholders, not usually directly filled with strings. To give state the preparedstatement is used
//		establishing database connection
		try (Connection conn = DBConnection.getConnection(); 
//		pre-compiled sql statement that prevents SQL injection by treating user input as data.
				
//				binding data to placeholders
				PreparedStatement stmt = conn.prepareStatement(sql)){
			stmt.setString(1,  user.getEmail());
			stmt.setString(2,  user.getPassword());
			stmt.setString(3,  user.getRole());
			stmt.setString(4,  user.getSecurityQuestion());
			stmt.setString(5,  user.getSecurityAnswer());
			
//			executeUpdate is used for insert, update, and delete, return a number of rows changed.
			int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
		}
		catch (SQLException e)
		{
			if (e.getErrorCode() == 1) 
			{
		        System.out.println("[Database Log]: Attempted to register a duplicate email.");
		    } else 
		    {
		        e.printStackTrace();
		    }
		    return false;
		}
		
	}

	@Override
	public User loginUser(String email, String password) {
		String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
		        
//		db connection
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) 
        {
            
        pstmt.setString(1, email);
        pstmt.setString(2, password);
        
//        executeQuery returns a ResultSet, a temporary table held in memory that contains the results of your search.
	        try (ResultSet rs = pstmt.executeQuery()) 
	        {
//	        to know who logged in the user details are stored in object User	
//	        the ResultSet when created, sits above the first row and on calling .next() moves the cursor down to first row of data. Till the method return true or null it moves.
	            if (rs.next()) 
	            {
	                User user = new User();
	                user.setUserId(rs.getInt("user_id"));
	                user.setEmail(rs.getString("email"));
	                user.setRole(rs.getString("role"));
	                user.setProfileCompletionPercent(rs.getInt("profile_completion_percent"));
	                return user;
	            }
	        }
        } catch (SQLException e) 
        {
            e.printStackTrace();
        }
	        return null; // Login failed
	}
	
//	Forgot password

	@Override
	public String getSecurityQuestion(String email) 
	{
		String sql = "SELECT security_question FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) 
        {
            
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery())
            {
                if (rs.next()) 
                {
                    return rs.getString("security_question");
                }
            }
        } catch (SQLException e) 
        {
            e.printStackTrace();
        }
        return null; 
//        either email doesn't exist or sql exception occured.
        
	}

	@Override
	public boolean validateSecurityAnswer(String email, String answer) 
	{
		String sql = "SELECT 1 FROM users WHERE email = ? AND UPPER(security_answer) = UPPER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) 
        {
            
            pstmt.setString(1, email);
            pstmt.setString(2, answer);
            
            try (ResultSet rs = pstmt.executeQuery()) 
            {
                return rs.next(); // Returns true if a match is found
            }
        } catch (SQLException e) 
        {
            e.printStackTrace();
		return false;
        }
	}

	@Override
	public boolean updatePassword(String email, String newPassword) 
	{
		String sql = "UPDATE users SET password = ? WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) 
        {
            
            pstmt.setString(1, newPassword);
            pstmt.setString(2, email);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) 
        {
            e.printStackTrace();
            return false;
        }
    }
	
	@Override
	public boolean saveEmployerProfile(EmployerProfile profile) {
	    String sql = "MERGE INTO employer_profile p " +
	                 "USING (SELECT ? as user_id FROM dual) s " +
	                 "ON (p.user_id = s.user_id) " +
	                 "WHEN MATCHED THEN " +
	                 "  UPDATE SET company_name = ?, industry = ?, company_size = ?, description = ?, website = ?, location = ? " +
	                 "WHEN NOT MATCHED THEN " +
	                 "  INSERT (user_id, company_name, industry, company_size, description, website, location) " +
	                 "  VALUES (?, ?, ?, ?, ?, ?, ?)";

	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {
	        
	        stmt.setInt(1, profile.getUserId()); // Match condition
	        
	        // Update part
	        stmt.setString(2, profile.getCompanyName());
	        stmt.setString(3, profile.getIndustry());
	        stmt.setString(4, profile.getCompanySize());
	        stmt.setString(5, profile.getDescription());
	        stmt.setString(6, profile.getWebsite());
	        stmt.setString(7, profile.getLocation());
	        
	        // Insert part
	        stmt.setInt(8, profile.getUserId());
	        stmt.setString(9, profile.getCompanyName());
	        stmt.setString(10, profile.getIndustry());
	        stmt.setString(11, profile.getCompanySize());
	        stmt.setString(12, profile.getDescription());
	        stmt.setString(13, profile.getWebsite());
	        stmt.setString(14, profile.getLocation());

	        return stmt.executeUpdate() > 0;
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}

	@Override
	public EmployerProfile getEmployerProfile(int userId) {
	    String sql = "SELECT * FROM employer_profile WHERE user_id = ?";
	    EmployerProfile profile = null;

	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {
	        
	        stmt.setInt(1, userId);
	        ResultSet rs = stmt.executeQuery();

	        if (rs.next()) {
	            profile = new EmployerProfile();
	            profile.setUserId(rs.getInt("user_id"));
	            profile.setCompanyName(rs.getString("company_name"));
	            profile.setIndustry(rs.getString("industry"));
	            profile.setCompanySize(rs.getString("company_size"));
	            profile.setDescription(rs.getString("description"));
	            profile.setWebsite(rs.getString("website"));
	            profile.setLocation(rs.getString("location"));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return profile; // Returns null if no profile exists yet
	}
	
	@Override
	public boolean validateOldPassword(int userId, String oldPassword) {
	    String sql = "SELECT count(*) FROM users WHERE user_id = ? AND password = ?";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setInt(1, userId);
	        stmt.setString(2, oldPassword);
	        ResultSet rs = stmt.executeQuery();
	        if (rs.next()) {
	            return rs.getInt(1) > 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}

	@Override
	public boolean updatePassword(int userId, String newPassword) {
	    String sql = "UPDATE users SET password = ? WHERE user_id = ?";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setString(1, newPassword);
	        stmt.setInt(2, userId);
	        return stmt.executeUpdate() > 0;
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}
	
	@Override
	public JobSeekerProfile getJobSeekerProfile(int userId) {
	    String sql = "SELECT * FROM job_seeker_profile WHERE user_id = ?";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {
	        
	        stmt.setInt(1, userId);
	        ResultSet rs = stmt.executeQuery();
	        
	        if (rs.next()) {
	            JobSeekerProfile p = new JobSeekerProfile();
	            p.setUserId(rs.getInt("user_id"));
	            p.setFullName(rs.getString("full_name"));
	            p.setPhone(rs.getString("phone"));
	            p.setLocation(rs.getString("location"));
	            				p.setTotalExperience(rs.getInt("total_experience")); 
	            
	            return p;
	        }
	    } catch (SQLException e) { 
	        e.printStackTrace(); 
	    }
	    return null;
	}

	@Override
	public boolean saveJobSeekerProfile(JobSeekerProfile p) {
	    // 1. Check if profile already exists
	    String checkSql = "SELECT COUNT(*) FROM job_seeker_profile WHERE user_id = ?";
	    
	    try (Connection conn = DBConnection.getConnection()) {
//	    	System.out.println("DEBUG: Connection secured! Preparing statement...");
	        boolean exists = false;
	        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
	            checkStmt.setInt(1, p.getUserId());
	            ResultSet rs = checkStmt.executeQuery();
	            if (rs.next()) {
	                exists = rs.getInt(1) > 0;
	            }
	        }
	
	        if (exists) {
	            // 2. Perform UPDATE
	            String updateSql = "UPDATE job_seeker_profile SET full_name = ?, phone = ?, " +
	                               "location = ?, total_experience = ? WHERE user_id = ?";
	            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
	                updateStmt.setString(1, p.getFullName());
	                updateStmt.setString(2, p.getPhone());
	                updateStmt.setString(3, p.getLocation());
	                updateStmt.setInt(4, p.getTotalExperience());
	                updateStmt.setInt(5, p.getUserId());
	                return updateStmt.executeUpdate() > 0;
	            }
	        } else {
	            // 3. Perform INSERT
	            String insertSql = "INSERT INTO job_seeker_profile (user_id, full_name, phone, " +
	                               "location, total_experience) VALUES (?, ?, ?, ?, ?)";
	            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
	                insertStmt.setInt(1, p.getUserId());
	                insertStmt.setString(2, p.getFullName());
	                insertStmt.setString(3, p.getPhone());
	                insertStmt.setString(4, p.getLocation());
	                insertStmt.setInt(5, p.getTotalExperience());
	                System.out.println("DEBUG: Executing update...");
	                int rows= insertStmt.executeUpdate();
	                System.out.println("DEBUG: Update finished. Rows affected: " + rows);
	                return rows > 0;
	                
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}
	
	@Override
	public User getUserById(int userId) {
	    String query = "SELECT user_id, email, role FROM users WHERE user_id = ?";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(query)) {
	        
	        ps.setInt(1, userId);
	        ResultSet rs = ps.executeQuery();
	        
	        if (rs.next()) {
	            User user = new User();
	            user.setUserId(rs.getInt("user_id"));
	            user.setEmail(rs.getString("email"));
	            user.setRole(rs.getString("role"));
	            return user;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null;
	}

}
