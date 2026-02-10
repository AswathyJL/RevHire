package com.pack.RevHire.model;

import java.sql.Timestamp;

public class JobApplication {

    private int applicationId;
    private int jobId;
    private int userId;
    private String status;
    private String coverLetter;
    private String withdrawalReason;
    private String employerComments;
    private Timestamp appliedAt;
	public int getApplicationId() {
		return applicationId;
	}
	public void setApplicationId(int applicationId) {
		this.applicationId = applicationId;
	}
	public int getJobId() {
		return jobId;
	}
	public void setJobId(int jobId) {
		this.jobId = jobId;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getCoverLetter() {
		return coverLetter;
	}
	public void setCoverLetter(String coverLetter) {
		this.coverLetter = coverLetter;
	}
	public String getWithdrawalReason() {
		return withdrawalReason;
	}
	public void setWithdrawalReason(String withdrawalReason) {
		this.withdrawalReason = withdrawalReason;
	}
	public String getEmployerComments() {
		return employerComments;
	}
	public void setEmployerComments(String employerComments) {
		this.employerComments = employerComments;
	}
	public Timestamp getAppliedAt() {
		return appliedAt;
	}
	public void setAppliedAt(Timestamp appliedAt) {
		this.appliedAt = appliedAt;
	}

    // getters and setters
}
