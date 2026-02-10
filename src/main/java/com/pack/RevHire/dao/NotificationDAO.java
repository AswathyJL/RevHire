package com.pack.RevHire.dao;

import java.util.List;
import java.util.Map;

import com.pack.RevHire.model.Notification;

public interface NotificationDAO {
	
	Map<String, Object> getNotificationContext(int appId);
	boolean createNotification(int userId, String message);

	List<Notification> getNotificationsByUserId(int userId);
	boolean markAsRead(int notificationId);
	boolean deleteNotification(int notificationId);
	
	
//	getting details of employer who posted the job.
	int getEmployerIdByJobId(int jobId);
	String getJobTitleById(int jobId);
	boolean bulkDelete(List<Integer> ids);
	boolean bulkMarkAsRead(List<Integer> ids);
	
//	to get matching job seeker to job skills
	List<Integer> getMatchingSeekers(int jobId);
	boolean bulkMarkAsRead(List<Integer> ids, int userId);
	boolean bulkDelete(List<Integer> ids, int userId);
	
}
