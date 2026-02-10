package com.pack.RevHire.service;

import com.pack.RevHire.dao.UserDAO;
import com.pack.RevHire.dao.implementation.UserDAOImpl;
import com.pack.RevHire.model.EmployerProfile;
import com.pack.RevHire.model.JobSeekerProfile;
import com.pack.RevHire.model.User;

public class UserService {
	private UserDAO userDAO = new UserDAOImpl();

    public String registerUser(User user) {
        // 1. Validation Logic
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            return "Invalid email address.";
        }
        if (user.getPassword().length() < 6) {
            return "Password must be at least 6 characters.";
        }

        // 2. Call DAO
        boolean isSaved = userDAO.registerUser(user);
        
        return isSaved ? "SUCCESS" : "FAILURE: Email might already be registered.";
    }

    public User login(String email, String password) {
        // Business Rule: Don't even hit the DB if fields are empty
        if (email.isEmpty() || password.isEmpty()) {
            return null;
        }
        return userDAO.loginUser(email, password);
    }
    
//    security question
    public String getSecurityQuestion(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Email cannot be empty.";
        }
        String question = userDAO.getSecurityQuestion(email);
        return (question != null) ? question : "User not found.";
    }
    
//    security answer
    public boolean verifySecurityAnswer(String email, String answer) {
        if (answer == null || answer.trim().isEmpty()) {
            return false;
        }
        return userDAO.validateSecurityAnswer(email, answer);
    }
    
//    forget password
    public String resetPassword(String email, String newPassword) {
        if (newPassword.length() < 6) {
            return "New password is too weak (min 6 characters).";
        }
        
        boolean isUpdated = userDAO.updatePassword(email, newPassword);
        return isUpdated ? "Password updated successfully!" : "Failed to update password.";
    }
    
    public boolean saveEmployerProfile(EmployerProfile profile) {
        // Business logic: check if company name is valid
        if (profile.getCompanyName() == null || profile.getCompanyName().isEmpty()) {
            return false;
        }
        return userDAO.saveEmployerProfile(profile);
    }
    
    public EmployerProfile getEmployerProfile(int userId) {
        return userDAO.getEmployerProfile(userId);
    }
    
    public boolean updateUserPassword(int userId, String oldPassword, String newPassword) {
        // Business logic: check if old password is correct first
        if (userDAO.validateOldPassword(userId, oldPassword)) {
            return userDAO.updatePassword(userId, newPassword);
        }
        return false;
    }
    
    public JobSeekerProfile getJobSeekerProfile(int userId) {
        return userDAO.getJobSeekerProfile(userId);
    }

    public boolean saveJobSeekerProfile(JobSeekerProfile profile) {
        // Validation: Ensure name isn't blank
        if (profile.getFullName() == null || profile.getFullName().isEmpty()) return false;
        return userDAO.saveJobSeekerProfile(profile);
    }
    
    public String getUserEmail(int userId) {
        // We can fetch the full User object and get the email
        User user = userDAO.getUserById(userId); 
        return (user != null) ? user.getEmail() : "N/A";
    }
    
}
