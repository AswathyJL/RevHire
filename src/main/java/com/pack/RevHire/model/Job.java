package com.pack.RevHire.model;

import java.sql.Date;
import java.sql.Timestamp;

public class Job {

    private int jobId;
    private int employerId;
    private String title;
    private String description;
    private int experienceRequired;
    private String educationRequired;
    private String location;
    private double salaryMin;
    private double salaryMax;
    private String jobType;
    private Date deadline;
    private String status;
    private Timestamp createdAt;
    private String companyName;

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
	public int getJobId() {
		return jobId;
	}
	public void setJobId(int jobId) {
		this.jobId = jobId;
	}
	public int getEmployerId() {
		return employerId;
	}
	public void setEmployerId(int employerId) {
		this.employerId = employerId;
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
	public int getExperienceRequired() {
		return experienceRequired;
	}
	public void setExperienceRequired(int experienceRequired) {
		this.experienceRequired = experienceRequired;
	}
	public String getEducationRequired() {
		return educationRequired;
	}
	public void setEducationRequired(String educationRequired) {
		this.educationRequired = educationRequired;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public double getSalaryMin() {
		return salaryMin;
	}
	public void setSalaryMin(double salaryMin) {
		this.salaryMin = salaryMin;
	}
	public double getSalaryMax() {
		return salaryMax;
	}
	public void setSalaryMax(double salaryMax) {
		this.salaryMax = salaryMax;
	}
	public String getJobType() {
		return jobType;
	}
	public void setJobType(String jobType) {
		this.jobType = jobType;
	}
	public Date getDeadline() {
		return deadline;
	}
	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Timestamp getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

    // getters and setters
    
}
