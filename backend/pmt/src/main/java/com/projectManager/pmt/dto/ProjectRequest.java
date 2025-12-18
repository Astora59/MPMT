package com.projectManager.pmt.dto;

public class ProjectRequest {

    private String projectName;
    private String projectDescription;


    // Getters et Setters
    public String getProjectName() {
        return projectName;
    }
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectDescription() {
        return projectDescription;
    }
    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

}
