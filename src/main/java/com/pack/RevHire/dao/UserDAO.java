package com.pack.RevHire.dao;

import com.pack.RevHire.model.EmployerProfile;
import com.pack.RevHire.model.JobSeekerProfile;
import com.pack.RevHire.model.User;

public interface UserDAO 
{
	//checks if registration successfull
    boolean registerUser(User user);

    // credentials match, null otherwise
    User loginUser(String email, String password);

    // For forgot password logic
    String getSecurityQuestion(String email);
    boolean validateSecurityAnswer(String email, String answer);
    boolean updatePassword(String email, String newPassword);
    boolean saveEmployerProfile(EmployerProfile profile);
    EmployerProfile getEmployerProfile(int userId);
    
    boolean validateOldPassword(int userId, String oldPassword);
    boolean updatePassword(int userId, String newPassword);
    
    JobSeekerProfile getJobSeekerProfile(int userId);
    boolean saveJobSeekerProfile(JobSeekerProfile profile);
    User getUserById(int userId);
}
