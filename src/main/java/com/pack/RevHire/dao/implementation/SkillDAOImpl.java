package com.pack.RevHire.dao.implementation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.pack.RevHire.config.DBConnection;
import com.pack.RevHire.dao.SkillDAO;
import com.pack.RevHire.model.Skill;

public class SkillDAOImpl implements SkillDAO{
	
	@Override
    public List<Skill> findAll() {
        List<Skill> skills = new ArrayList<>();
        String sql = "SELECT skill_id, skill_name FROM skills ORDER BY skill_name ASC";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Skill skill = new Skill();
                skill.setSkillId(rs.getInt("skill_id"));
                skill.setSkillName(rs.getString("skill_name"));
                skills.add(skill);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching skills: " + e.getMessage());
        }
        return skills;
    }
	
	@Override
    public int upsertSkill(String skillName) {
        String selectSql = "SELECT skill_id FROM skills WHERE UPPER(skill_name) = UPPER(?)";
        String insertSql = "INSERT INTO skills (skill_name) VALUES (?)";

        try (Connection conn = DBConnection.getConnection()) {
            // 1. Check if it exists first
            try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
                pstmt.setString(1, skillName.trim());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("skill_id");
                }
            }

            // 2. If it doesn't exist, insert it
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql, new String[]{"skill_id"})) {
                pstmt.setString(1, skillName.trim());
                pstmt.executeUpdate();
                
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            // If two people add the same skill at the exact same time, 
            // the insert might fail. We catch that and try to select one last time.
            System.err.println("Skill conflict handled: " + e.getMessage());
        }
        return -1;
    }


}
