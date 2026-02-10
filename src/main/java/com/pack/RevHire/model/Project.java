package com.pack.RevHire.model;


public class Project {

    private int projectId;
    private int resumeId;
    private String title;
    private String description;
    private String techStack;
	public int getProjectId() {
		return projectId;
	}
	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}
	public int getResumeId() {
		return resumeId;
	}
	public void setResumeId(int resumeId) {
		this.resumeId = resumeId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getTechStack() {
		return techStack;
	}
	public void setTechStack(String techStack) {
		this.techStack = techStack;
	}

    // getters and setters
}
