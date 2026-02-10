package com.pack.RevHire.service;

import java.util.List;

import com.pack.RevHire.dao.SkillDAO;
import com.pack.RevHire.dao.implementation.SkillDAOImpl;
import com.pack.RevHire.model.Skill;

public class SkillService {
	
	private SkillDAO skillDAO = new SkillDAOImpl();
	
	 /**
     * Retrieves the master list of skills for the View to display.
     */
    public List<Skill> getAllSkills() {
        return skillDAO.findAll();
    }
    
    /**
     * Handles new skill creation. 
     * If the skill exists, returns the ID. If not, creates it.
     */
    public int getOrInsertSkill(String skillName) {
        if (skillName == null || skillName.trim().isEmpty()) {
            System.out.println(">>> Error: Skill name cannot be empty.");
            return -1;
        }
        // Delegates to DAO logic to ensure uniqueness
        return skillDAO.upsertSkill(skillName);
    }
    

}
