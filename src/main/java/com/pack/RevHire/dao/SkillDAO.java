package com.pack.RevHire.dao;

import java.util.List;

import com.pack.RevHire.model.Skill;

public interface SkillDAO {
	
	List<Skill> findAll();
    int upsertSkill(String skillName);

}
