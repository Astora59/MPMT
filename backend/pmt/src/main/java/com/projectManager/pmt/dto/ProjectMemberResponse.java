package com.projectManager.pmt.dto;

import java.util.UUID;

public class ProjectMemberResponse {

    private UUID userId;
    private String username;
    private String email;
    private String roleName;

    public ProjectMemberResponse(UUID userId, String username, String email, String roleName) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.roleName = roleName;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getRoleName() {
        return roleName;
    }
}