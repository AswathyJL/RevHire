package com.pack.RevHire.model;
import java.sql.Date;

public class Certification {

    private int certId;
    private int resumeId;
    private String certName;
    private String issuingOrganization;
    private Date issueDate;
	public int getCertId() {
		return certId;
	}
	public void setCertId(int certId) {
		this.certId = certId;
	}
	public int getResumeId() {
		return resumeId;
	}
	public void setResumeId(int resumeId) {
		this.resumeId = resumeId;
	}
	public String getCertName() {
		return certName;
	}
	public void setCertName(String certName) {
		this.certName = certName;
	}
	public String getIssuingOrganization() {
		return issuingOrganization;
	}
	public void setIssuingOrganization(String issuingOrganization) {
		this.issuingOrganization = issuingOrganization;
	}
	public Date getIssueDate() {
		return issueDate;
	}
	public void setIssueDate(Date issueDate) {
		this.issueDate = issueDate;
	}

    // getters and setters
}
