package com.projectManager.pmt.dto;

public class TaskCreationResponse {
    private String taskTitle;
    private String taskDescription;
    private String taskStatus;

    public TaskCreationResponse(String taskTitle, String taskDescription, String taskStatus) {
        this.taskTitle = taskTitle;
        this.taskDescription = taskDescription;
        this.taskStatus = taskStatus;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public String getTaskStatus() {
        return taskStatus;
    }
}
