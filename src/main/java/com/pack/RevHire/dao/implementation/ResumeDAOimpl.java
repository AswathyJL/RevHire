package com.pack.RevHire.dao.implementation;

import java.sql.Connection;
import java.util.*;
import java.sql.*;

import com.pack.RevHire.config.DBConnection;
import com.pack.RevHire.dao.ResumeDAO;
import com.pack.RevHire.model.Certification;
import com.pack.RevHire.model.Education;
import com.pack.RevHire.model.Experience;
import com.pack.RevHire.model.Project;
import com.pack.RevHire.model.Skill;

public class ResumeDAOimpl implements ResumeDAO{

	@Override
	public Integer getResumeIdByUserId(int userId) {
	    String sql = "SELECT resume_id FROM resume WHERE user_id = ?";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, userId);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getInt("resume_id");
	        }
	    } catch (SQLException e) { e.printStackTrace(); }
	    return null;
	}

	@Override
	public int createResume(int userId) {
	    String sql = "INSERT INTO resume (user_id) VALUES (?)";
	    String generatedColumns[] = {"resume_id"};
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql, generatedColumns)) {
	        pstmt.setInt(1, userId);
	        pstmt.executeUpdate();
	        
	        ResultSet rs = pstmt.getGeneratedKeys();
	        if (rs.next()) {
	            return rs.getInt(1);
	        }
	    } catch (SQLException e) { e.printStackTrace(); }
	    return -1;
	}
	
//	OBJECTIVE

	@Override
	public boolean updateObjective(int resumeId, String objective) {
	    String sql = "UPDATE resume SET objective = ? WHERE resume_id = ?";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        
	        pstmt.setString(1, objective);
	        pstmt.setInt(2, resumeId);
	        
	        return pstmt.executeUpdate() > 0;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}

	@Override
	public String getObjective(int resumeId) {
	    String sql = "SELECT objective FROM resume WHERE resume_id = ?";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        
	        pstmt.setInt(1, resumeId);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("objective");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	
//	SKILLS
	
	@Override
	public List<Skill> getSkillsByResumeId(int resumeId) {
	    List<Skill> skills = new ArrayList<>();
	    String sql = "SELECT s.skill_id, s.skill_name FROM skills s " +
	                 "JOIN resume_skills rs ON s.skill_id = rs.skill_id " +
	                 "WHERE rs.resume_id = ?";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, resumeId);
	        ResultSet rs = pstmt.executeQuery();
	        while (rs.next()) {
	            Skill s = new Skill();
	            s.setSkillId(rs.getInt("skill_id"));
	            s.setSkillName(rs.getString("skill_name"));
	            skills.add(s);
	        }
	    } catch (SQLException e) { e.printStackTrace(); }
	    return skills;
	}

	@Override
	public boolean linkSkillToResume(int resumeId, int skillId) {
	    // Using MERGE to prevent primary key violations if the user adds the same skill twice
	    String sql = "MERGE INTO resume_skills rs " +
	                 "USING DUAL ON (rs.resume_id = ? AND rs.skill_id = ?) " +
	                 "WHEN NOT MATCHED THEN INSERT (resume_id, skill_id) VALUES (?, ?)";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, resumeId);
	        pstmt.setInt(2, skillId);
	        pstmt.setInt(3, resumeId);
	        pstmt.setInt(4, skillId);
	        return pstmt.executeUpdate() > 0;
	    } catch (SQLException e) { e.printStackTrace(); }
	    return false;
	}

	@Override
	public boolean unlinkSkillFromResume(int resumeId, int skillId) {
	    String sql = "DELETE FROM resume_skills WHERE resume_id = ? AND skill_id = ?";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, resumeId);
	        pstmt.setInt(2, skillId);
	        return pstmt.executeUpdate() > 0;
	    } catch (SQLException e) { e.printStackTrace(); }
	    return false;
	}
	
//	EDUCATION
	
	@Override
	public List<Education> getEducationByResumeId(int resumeId) {
	    List<Education> eduList = new ArrayList<>();
	    // Note: The sorting requirement is handled here by SQL
	    String sql = "SELECT * FROM education WHERE resume_id = ? " +
                "ORDER BY CASE WHEN end_year = 0 THEN 9999 ELSE end_year END DESC";
	    
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        
	        pstmt.setInt(1, resumeId);
	        ResultSet rs = pstmt.executeQuery();
	        
	        while (rs.next()) {
	            Education edu = new Education();
	            edu.setEducationId(rs.getInt("education_id"));
	            edu.setResumeId(rs.getInt("resume_id"));
	            edu.setDegree(rs.getString("degree"));
	            edu.setInstitution(rs.getString("institution"));
	            edu.setStartYear(rs.getInt("start_year"));
	            edu.setEndYear(rs.getInt("end_year"));
	            eduList.add(edu);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return eduList;
	}

	@Override
	public Education getEducationById(int eduId) {
	    String sql = "SELECT * FROM education WHERE education_id = ?";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        
	        pstmt.setInt(1, eduId);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            Education edu = new Education();
	            edu.setEducationId(rs.getInt("education_id"));
	            edu.setResumeId(rs.getInt("resume_id"));
	            edu.setDegree(rs.getString("degree"));
	            edu.setInstitution(rs.getString("institution"));
	            edu.setStartYear(rs.getInt("start_year"));
	            edu.setEndYear(rs.getInt("end_year"));
	            return edu;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null;
	}

	@Override
	public boolean insertEducation(Education edu) {
	    String sql = "INSERT INTO education (resume_id, degree, institution, start_year, end_year) VALUES (?, ?, ?, ?, ?)";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        
	        pstmt.setInt(1, edu.getResumeId());
	        pstmt.setString(2, edu.getDegree());
	        pstmt.setString(3, edu.getInstitution());
	        pstmt.setInt(4, edu.getStartYear());
	        pstmt.setInt(5, edu.getEndYear());
	        
	        return pstmt.executeUpdate() > 0;
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}

	@Override
	public boolean updateEducation(Education edu) {
	    String sql = "UPDATE education SET degree = ?, institution = ?, start_year = ?, end_year = ? WHERE education_id = ?";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        
	        pstmt.setString(1, edu.getDegree());
	        pstmt.setString(2, edu.getInstitution());
	        pstmt.setInt(3, edu.getStartYear());
	        pstmt.setInt(4, edu.getEndYear());
	        pstmt.setInt(5, edu.getEducationId());
	        
	        return pstmt.executeUpdate() > 0;
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}

	@Override
	public boolean deleteEducation(int eduId) {
	    String sql = "DELETE FROM education WHERE education_id = ?";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        
	        pstmt.setInt(1, eduId);
	        return pstmt.executeUpdate() > 0;
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}
	
//	EXPERIENCE
	
	@Override
	public List<Experience> getExperienceByResumeId(int resumeId) {
	    List<Experience> list = new ArrayList<>();
	    // Sorting: Records with NULL end_date (Present) come first, 
	    // then others sorted by most recent end_date.
	    String sql = "SELECT * FROM experience WHERE resume_id = ? " +
	                 "ORDER BY (CASE WHEN end_date IS NULL THEN 1 ELSE 0 END) DESC, end_date DESC";

	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        
	        pstmt.setInt(1, resumeId);
	        ResultSet rs = pstmt.executeQuery();
	        while (rs.next()) {
	            Experience exp = new Experience();
	            exp.setExperienceId(rs.getInt("experience_id"));
	            exp.setResumeId(rs.getInt("resume_id"));
	            exp.setCompany(rs.getString("company"));
	            exp.setJobRole(rs.getString("job_role"));
	            exp.setStartDate(rs.getDate("start_date"));
	            exp.setEndDate(rs.getDate("end_date")); // May be null
	            exp.setDescription(rs.getString("description"));
	            list.add(exp);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return list;
	}

	@Override
	public Experience getExperienceById(int expId) {
	    String sql = "SELECT * FROM experience WHERE experience_id = ?";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        
	        pstmt.setInt(1, expId);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            Experience exp = new Experience();
	            exp.setExperienceId(rs.getInt("experience_id"));
	            exp.setResumeId(rs.getInt("resume_id"));
	            exp.setCompany(rs.getString("company"));
	            exp.setJobRole(rs.getString("job_role"));
	            exp.setStartDate(rs.getDate("start_date"));
	            exp.setEndDate(rs.getDate("end_date"));
	            exp.setDescription(rs.getString("description"));
	            return exp;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null;
	}

	@Override
	public boolean insertExperience(Experience exp) {
	    String sql = "INSERT INTO experience (resume_id, company, job_role, start_date, end_date, description) VALUES (?, ?, ?, ?, ?, ?)";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        
	        pstmt.setInt(1, exp.getResumeId());
	        pstmt.setString(2, exp.getCompany());
	        pstmt.setString(3, exp.getJobRole());
	        pstmt.setDate(4, exp.getStartDate());
	        pstmt.setDate(5, exp.getEndDate()); // JDBC handles null automatically
	        pstmt.setString(6, exp.getDescription());
	        
	        return pstmt.executeUpdate() > 0;
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}

	@Override
	public boolean updateExperience(Experience exp) {
	    String sql = "UPDATE experience SET company = ?, job_role = ?, start_date = ?, end_date = ?, description = ? WHERE experience_id = ?";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        
	        pstmt.setString(1, exp.getCompany());
	        pstmt.setString(2, exp.getJobRole());
	        pstmt.setDate(3, exp.getStartDate());
	        pstmt.setDate(4, exp.getEndDate());
	        pstmt.setString(5, exp.getDescription());
	        pstmt.setInt(6, exp.getExperienceId());
	        
	        return pstmt.executeUpdate() > 0;
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}

	@Override
	public boolean deleteExperience(int expId) {
	    String sql = "DELETE FROM experience WHERE experience_id = ?";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        
	        pstmt.setInt(1, expId);
	        return pstmt.executeUpdate() > 0;
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}
	
//	PROJECTS
	
	@Override
	public List<Project> getProjectsByResumeId(int resumeId) {
	    List<Project> list = new ArrayList<>();
	    String sql = "SELECT * FROM projects WHERE resume_id = ?";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, resumeId);
	        ResultSet rs = pstmt.executeQuery();
	        while (rs.next()) {
	            Project p = new Project();
	            p.setProjectId(rs.getInt("project_id"));
	            p.setResumeId(rs.getInt("resume_id"));
	            p.setTitle(rs.getString("title"));
	            p.setDescription(rs.getString("description"));
	            p.setTechStack(rs.getString("tech_stack"));
	            list.add(p);
	        }
	    } catch (SQLException e) { e.printStackTrace(); }
	    return list;
	}

	@Override
	public boolean insertProject(Project project) {
	    String sql = "INSERT INTO projects (resume_id, title, description, tech_stack) VALUES (?, ?, ?, ?)";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, project.getResumeId());
	        pstmt.setString(2, project.getTitle());
	        pstmt.setString(3, project.getDescription());
	        pstmt.setString(4, project.getTechStack());
	        return pstmt.executeUpdate() > 0;
	    } catch (SQLException e) { e.printStackTrace(); return false; }
	}

	@Override
	public boolean updateProject(Project project) {
	    // This SQL handles the title, description, and the new tech stack string
	    String sql = "UPDATE projects SET title = ?, description = ?, tech_stack = ? WHERE project_id = ?";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        
	        pstmt.setString(1, project.getTitle());
	        pstmt.setString(2, project.getDescription());
	        pstmt.setString(3, project.getTechStack()); // String like "Java, Spring, SQL"
	        pstmt.setInt(4, project.getProjectId());
	        
	        return pstmt.executeUpdate() > 0;
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}

	@Override
	public boolean deleteProject(int projectId) {
	    String sql = "DELETE FROM projects WHERE project_id = ?";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, projectId);
	        return pstmt.executeUpdate() > 0;
	    } catch (SQLException e) { e.printStackTrace(); return false; }
	}

	@Override
	public Project getProjectById(int projectId) {
	    String sql = "SELECT * FROM projects WHERE project_id = ?";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        
	        pstmt.setInt(1, projectId);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            Project p = new Project();
	            p.setProjectId(rs.getInt("project_id"));
	            p.setResumeId(rs.getInt("resume_id"));
	            p.setTitle(rs.getString("title"));
	            p.setDescription(rs.getString("description"));
	            p.setTechStack(rs.getString("tech_stack"));
	            return p;
	        }
	    } catch (SQLException e) {
	        System.err.println(">>> DAO Error: Could not fetch project by ID.");
	        e.printStackTrace();
	    }
	    return null; // Returns null if the project doesn't exist
	}
	
//	CERTIFICATION
	
	@Override
	public List<Certification> getCertificationsByResumeId(int resumeId) {
	    List<Certification> list = new ArrayList<>();
	    String sql = "SELECT * FROM certifications WHERE resume_id = ? ORDER BY issue_date DESC";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, resumeId);
	        ResultSet rs = pstmt.executeQuery();
	        while (rs.next()) {
	            Certification c = new Certification();
	            c.setCertId(rs.getInt("cert_id"));
	            c.setResumeId(rs.getInt("resume_id"));
	            c.setCertName(rs.getString("cert_name"));
	            c.setIssuingOrganization(rs.getString("issuing_organization"));
	            c.setIssueDate(rs.getDate("issue_date"));
	            list.add(c);
	        }
	    } catch (SQLException e) { e.printStackTrace(); }
	    return list;
	}

	@Override
	public Certification getCertificationById(int certId) {
	    String sql = "SELECT * FROM certifications WHERE cert_id = ?";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, certId);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            Certification c = new Certification();
	            c.setCertId(rs.getInt("cert_id"));
	            c.setResumeId(rs.getInt("resume_id"));
	            c.setCertName(rs.getString("cert_name"));
	            c.setIssuingOrganization(rs.getString("issuing_organization"));
	            c.setIssueDate(rs.getDate("issue_date"));
	            return c;
	        }
	    } catch (SQLException e) { e.printStackTrace(); }
	    return null;
	}

	@Override
	public boolean insertCertification(Certification cert) {
	    String sql = "INSERT INTO certifications (resume_id, cert_name, issuing_organization, issue_date) VALUES (?, ?, ?, ?)";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, cert.getResumeId());
	        pstmt.setString(2, cert.getCertName());
	        pstmt.setString(3, cert.getIssuingOrganization());
	        pstmt.setDate(4, cert.getIssueDate());
	        return pstmt.executeUpdate() > 0;
	    } catch (SQLException e) { e.printStackTrace(); return false; }
	}

	@Override
	public boolean updateCertification(Certification cert) {
	    String sql = "UPDATE certifications SET cert_name = ?, issuing_organization = ?, issue_date = ? WHERE cert_id = ?";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setString(1, cert.getCertName());
	        pstmt.setString(2, cert.getIssuingOrganization());
	        pstmt.setDate(3, cert.getIssueDate());
	        pstmt.setInt(4, cert.getCertId());
	        return pstmt.executeUpdate() > 0;
	    } catch (SQLException e) { e.printStackTrace(); return false; }
	}

	@Override
	public boolean deleteCertification(int certId) {
	    String sql = "DELETE FROM certifications WHERE cert_id = ?";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, certId);
	        return pstmt.executeUpdate() > 0;
	    } catch (SQLException e) { e.printStackTrace(); return false; }
	}
}
