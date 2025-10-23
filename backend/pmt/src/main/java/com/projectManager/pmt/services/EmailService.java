package com.projectManager.pmt.services;



public interface EmailService {
    void sendTaskAssignedEmail(String recipientEmail, String taskTitle, String projectName, String assignedBy);
}
